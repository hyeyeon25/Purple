package Purple.Purple.Neighborhood.service;

import Purple.Purple.common.constants.TagDictionary;
import Purple.Purple.Neighborhood.dto.NeighborhoodRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PagedPlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.UserPreferenceRequestDto;
import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.Neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeighborhoodRecommendationServiceImpl implements NeighborhoodRecommendationService {

    private final NeighborhoodRepository neighborhoodRepository;
    private final PlaceRepository placeRepository;
    private final PreferencesRepository preferencesRepository;
    private final ObjectMapper objectMapper;
    private final Purple.Purple.place.service.PlaceService placeService;

    @Override
    @Transactional(readOnly = true)
    public List<NeighborhoodRecommendationResponseDto> recommendTop3NeighborhoodsByUserId(Long userId) {
        log.info("Starting neighborhood recommendation for userId: {}", userId);

        // 1. 사용자 선호도 벡터 조회
        PreferencesEntity preferences = preferencesRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 선호도 정보를 찾을 수 없습니다. userId: " + userId));

        if (preferences.getTagVector() == null || preferences.getTagVector().isEmpty()) {
            throw new IllegalStateException("사용자의 선호도 벡터가 생성되지 않았습니다. userId: " + userId);
        }

        // 2. JSON에서 벡터 파싱
        List<Double> normalizedUserVector;
        try {
            normalizedUserVector = parseVectorFromJson(preferences.getTagVector());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("사용자 벡터 파싱 중 오류가 발생했습니다.", e);
        }

        log.debug("User preference vector loaded: {}", normalizedUserVector);

        // 3. 동네 추천 로직 실행
        return recommendNeighborhoodsWithVector(normalizedUserVector);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedPlaceRecommendationResponseDto recommendPlacesInNeighborhoodByUserId(
            Integer neighborhoodId,
            String category,
            Long userId,
            int page,
            int size) {

        log.info("Starting place recommendation for userId: {}, neighborhoodId: {}, category: {}, page: {}, size: {}",
                userId, neighborhoodId, category, page, size);

        // 1. 사용자 선호도 벡터 조회
        PreferencesEntity preferences = preferencesRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 선호도 정보를 찾을 수 없습니다. userId: " + userId));

        if (preferences.getTagVector() == null || preferences.getTagVector().isEmpty()) {
            throw new IllegalStateException("사용자의 선호도 벡터가 생성되지 않았습니다. userId: " + userId);
        }

        // 2. JSON에서 벡터 파싱
        List<Double> normalizedUserVector;
        try {
            normalizedUserVector = parseVectorFromJson(preferences.getTagVector());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("사용자 벡터 파싱 중 오류가 발생했습니다.", e);
        }

        log.debug("User preference vector loaded: {}", normalizedUserVector);

        // 3. 장소 추천 로직 실행 (페이지네이션)
        return recommendPlacesWithVector(neighborhoodId, category, normalizedUserVector, page, size);
    }

    /**
     * 벡터 기반 동네 추천 (내부 로직)
     */
    private List<NeighborhoodRecommendationResponseDto> recommendNeighborhoodsWithVector(List<Double> normalizedUserVector) {
        log.info("User preference vector (normalized) size: {}", normalizedUserVector.size());

        // 2. 모든 동네 조회
        List<NeighborhoodEntity> allNeighborhoods = neighborhoodRepository.findAll();
        log.info("Total neighborhoods found: {}", allNeighborhoods.size());
        
        if (allNeighborhoods.isEmpty()) {
            log.warn("No neighborhoods found in database");
            return Collections.emptyList();
        }

        List<NeighborhoodRecommendationResponseDto> recommendations = new ArrayList<>();
        int neighborhoodsWithNoPlaces = 0;
        int neighborhoodsWithNoTagVectors = 0;

        // 3. 각 동네별 평균 유사도 점수 계산
        for (NeighborhoodEntity neighborhood : allNeighborhoods) {
            // 해당 동네의 모든 장소 조회
            List<PlaceEntity> places = placeRepository.findByNeighborhood(neighborhood);

            if (places.isEmpty()) {
                log.debug("Neighborhood {} (ID: {}) has no places, skipping", 
                        neighborhood.getNeighborhoodName(), neighborhood.getNeighborhoodId());
                neighborhoodsWithNoPlaces++;
                continue;
            }

            log.debug("Neighborhood {} (ID: {}) has {} places", 
                    neighborhood.getNeighborhoodName(), neighborhood.getNeighborhoodId(), places.size());

            // 각 장소와의 코사인 유사도 계산
            List<Double> similarityScores = new ArrayList<>();
            int placesWithNoTagVector = 0;
            
            for (PlaceEntity place : places) {
                if (place.getTagVector() == null || place.getTagVector().isEmpty()) {
                    log.debug("Place {} (ID: {}) has no tag vector, skipping", 
                            place.getPlaceName(), place.getPlaceId());
                    placesWithNoTagVector++;
                    continue;
                }

                try {
                    List<Double> placeVector = parseVectorFromJson(place.getTagVector());
                    double similarity = calculateCosineSimilarity(normalizedUserVector, placeVector);
                    similarityScores.add(similarity);
                } catch (Exception e) {
                    log.warn("Failed to parse vector for place {} (ID: {}): {}", 
                            place.getPlaceName(), place.getPlaceId(), e.getMessage());
                    placesWithNoTagVector++;
                }
            }

            if (placesWithNoTagVector == places.size()) {
                log.debug("Neighborhood {} (ID: {}) has {} places but none have tag vectors", 
                        neighborhood.getNeighborhoodName(), neighborhood.getNeighborhoodId(), places.size());
                neighborhoodsWithNoTagVectors++;
            }

            // 평균 유사도 점수 계산
            if (!similarityScores.isEmpty()) {
                double averageScore = similarityScores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                recommendations.add(NeighborhoodRecommendationResponseDto.builder()
                        .neighborhoodId(neighborhood.getNeighborhoodId())
                        .neighborhoodName(neighborhood.getNeighborhoodName())
                        .averageSimilarityScore(averageScore)
                        .placeCount(similarityScores.size())
                        .build());

                log.debug("Neighborhood {} average similarity: {} (based on {} places)",
                        neighborhood.getNeighborhoodName(), averageScore, similarityScores.size());
            }
        }

        log.info("Recommendation summary: {} neighborhoods processed, {} with no places, {} with no tag vectors, {} valid recommendations",
                allNeighborhoods.size(), neighborhoodsWithNoPlaces, neighborhoodsWithNoTagVectors, recommendations.size());

        // 4. 평균 유사도 점수 기준 내림차순 정렬 후 Top 3 선택
        List<NeighborhoodRecommendationResponseDto> top3 = recommendations.stream()
                .sorted(Comparator.comparing(NeighborhoodRecommendationResponseDto::getAverageSimilarityScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 5. 순위 부여
        for (int i = 0; i < top3.size(); i++) {
            top3.get(i).setRank(i + 1);
        }

        if (top3.isEmpty()) {
            log.warn("No neighborhoods could be recommended. Possible reasons: " +
                    "1) No neighborhoods in database, " +
                    "2) No places in any neighborhood, " +
                    "3) No places have tag vectors");
        } else {
            log.info("Top 3 neighborhoods recommended: {}",
                    top3.stream()
                            .map(NeighborhoodRecommendationResponseDto::getNeighborhoodName)
                            .collect(Collectors.toList()));
        }

        return top3;
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
     * TagDictionary의 56개 태그 순서를 따름
     */
    private List<Double> generateVector(List<String> tags) {
        List<Double> vector = new ArrayList<>();

        for (String dictTag : TagDictionary.TAGS) {
            if (tags.contains(dictTag)) {
                vector.add(1.0);
            } else {
                vector.add(0.0);
            }
        }

        return vector;
    }

    /**
     * L2 정규화
     * TagVectorizationServiceImpl의 normalizeVector와 동일한 로직
     */
    private List<Double> normalizeVector(List<Double> vector) {
        // L2 norm 계산
        double l2Norm = Math.sqrt(vector.stream()
                .mapToDouble(v -> v * v)
                .sum());

        // 0으로 나누는 것을 방지
        if (l2Norm == 0) {
            return vector;
        }

        // 정규화
        return vector.stream()
                .map(v -> v / l2Norm)
                .collect(Collectors.toList());
    }

    /**
     * JSON 문자열을 List<Double>로 파싱
     */
    private List<Double> parseVectorFromJson(String vectorJson) throws JsonProcessingException {
        return objectMapper.readValue(vectorJson, new TypeReference<List<Double>>() {});
    }

    /**
     * 코사인 유사도 계산
     * cosine_similarity = (A · B) / (||A|| * ||B||)
     *
     * 두 벡터가 모두 정규화되어 있다면 (||A|| = ||B|| = 1)
     * cosine_similarity = A · B (내적)
     */
    private double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("벡터 차원이 일치하지 않습니다.");
        }

        // 두 벡터 모두 정규화되어 있으므로 내적만 계산
        double dotProduct = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
        }

        return dotProduct;
    }

    /**
     * 벡터 기반 장소 추천 (내부 로직, 페이지네이션 지원)
     */
    private PagedPlaceRecommendationResponseDto recommendPlacesWithVector(
            Integer neighborhoodId,
            String category,
            List<Double> normalizedUserVector,
            int page,
            int size) {

        log.info("Starting place recommendation for neighborhood ID: {}, category: {}, page: {}, size: {}",
                neighborhoodId, category, page, size);

        // 1. 동네 조회
        NeighborhoodEntity neighborhood = neighborhoodRepository.findById(neighborhoodId)
                .orElseThrow(() -> new IllegalArgumentException("동네를 찾을 수 없습니다. ID: " + neighborhoodId));

        // 2. 해당 동네의 모든 장소 조회
        List<PlaceEntity> places = placeRepository.findByNeighborhood(neighborhood);

        if (places.isEmpty()) {
            log.warn("No places found in neighborhood: {}", neighborhood.getNeighborhoodName());
            return createEmptyPage(page, size);
        }

        // 3. 카테고리로 필터링
        List<PlaceEntity> filteredPlaces = filterPlacesByCategory(places, category);

        if (filteredPlaces.isEmpty()) {
            log.warn("No places found matching category: {} in neighborhood: {}",
                    category, neighborhood.getNeighborhoodName());
            return createEmptyPage(page, size);
        }

        log.debug("Filtered {} places by category: {}", filteredPlaces.size(), category);
        log.debug("User preference vector (normalized): {}", normalizedUserVector);

        // 4. 각 장소와 코사인 유사도 계산 및 DTO 변환
        List<PlaceRecommendationResponseDto> recommendations = new ArrayList<>();

        for (PlaceEntity place : filteredPlaces) {
            if (place.getTagVector() == null || place.getTagVector().isEmpty()) {
                log.debug("Place {} has no tag vector, skipping", place.getPlaceName());
                continue;
            }

            try {
                // 장소 벡터 파싱
                List<Double> placeVector = parseVectorFromJson(place.getTagVector());

                // 코사인 유사도 계산
                double similarity = calculateCosineSimilarity(normalizedUserVector, placeVector);

                // DTO 변환
                PlaceRecommendationResponseDto dto = PlaceRecommendationResponseDto.builder()
                        .placeId(place.getPlaceId())
                        .kakaoPlaceId(place.getKakaoPlaceId())
                        .placeName(place.getPlaceName())
                        .placeCategory(place.getPlaceCategory())
                        .address(place.getAddress())
                        .latitude(place.getLatitude())
                        .longitude(place.getLongitude())
                        .tags(place.getTags())
                        .similarityScore(similarity)
                        .isIndoor(place.getIsIndoor())
                        .recommendedSlot(place.getRecommendedSlot())
                        .stayDurationMinutes(place.getStayDurationMinutes())
                        .summary(place.getSummary())
                        .build();

                recommendations.add(dto);

            } catch (Exception e) {
                log.warn("Failed to process place {}: {}", place.getPlaceName(), e.getMessage());
            }
        }

        // 5. 유사도 점수 기준 내림차순 정렬
        List<PlaceRecommendationResponseDto> sortedRecommendations = recommendations.stream()
                .sorted(Comparator.comparing(PlaceRecommendationResponseDto::getSimilarityScore).reversed())
                .collect(Collectors.toList());

        // 6. 페이지네이션 적용
        long totalElements = sortedRecommendations.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 페이지 번호 유효성 검사
        if (page < 0) {
            page = 0;
        }

        // 페이지 범위 계산
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, sortedRecommendations.size());

        // 페이지에 해당하는 데이터 추출
        List<PlaceRecommendationResponseDto> pagedContent;
        if (startIndex >= sortedRecommendations.size()) {
            pagedContent = Collections.emptyList();
        } else {
            pagedContent = sortedRecommendations.subList(startIndex, endIndex);
        }

        log.info("📋 페이지네이션 된 {}개 장소의 AI 요약 정보를 확인합니다...", pagedContent.size());

        for (PlaceRecommendationResponseDto dto : pagedContent) {
            try {
                // 1. PlaceService를 통해 분석 데이터 가져오기 (DB 없으면 -> 파이썬 -> DB저장 -> 반환)
                // dto.getPlaceName()을 넘겨서 이름으로 검색하게 합니다.
                var analysis = placeService.getAnalysisData(dto.getPlaceId(), dto.getPlaceName());

                // 2. 가져온 데이터가 있으면 DTO에 덮어씌우기
                if (analysis != null) {
                    dto.setSummary(analysis.getReviewSummary());
                    // 필요하다면 키워드나 한줄평도 여기서 dto.setKeywords(...) 등으로 넣을 수 있음
                }
            } catch (Exception e) {
                // 요약 하나 실패했다고 전체 에러 내지 말고, 로그만 찍고 넘어감 (사용자는 목록은 봐야 하니까)
                log.warn("⚠️ 장소 ID {} 요약 로딩 실패: {}", dto.getPlaceId(), e.getMessage());
            }
        }

        log.info("Recommended {} places in neighborhood {} (category: {}), page: {}/{}, total: {}",
                pagedContent.size(), neighborhood.getNeighborhoodName(), category, page + 1, totalPages, totalElements);

        // 7. 페이지네이션 응답 DTO 생성
        return PagedPlaceRecommendationResponseDto.builder()
                .content(pagedContent)
                .currentPage(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    /**
     * 빈 페이지 응답 생성
     */
    private PagedPlaceRecommendationResponseDto createEmptyPage(int page, int size) {
        return PagedPlaceRecommendationResponseDto.builder()
                .content(Collections.emptyList())
                .currentPage(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    /**
     * 카테고리로 장소 필터링
     *
     * 각 장소는 우선순위에 따라 하나의 카테고리에만 속합니다.
     * 우선순위: 카페 > 문화시설 > 야외활동 > 음식점 > 기타
     *
     * @param places 필터링할 장소 리스트
     * @param category 필터링 카테고리 ("음식점", "카페", "문화", "액티비티", "기타")
     * @return 필터링된 장소 리스트
     */
    private List<PlaceEntity> filterPlacesByCategory(List<PlaceEntity> places, String category) {
        if (category == null || category.trim().isEmpty()) {
            return places;
        }

        String categoryLower = category.toLowerCase().trim();

        return places.stream()
                .filter(place -> {
                    String placeCategory = place.getPlaceCategory();
                    if (placeCategory == null) {
                        return false;
                    }

                    String placeCategoryLower = placeCategory.toLowerCase();

                    // 장소의 실제 카테고리 결정 (우선순위 기반)
                    String actualCategory = determinePrimaryCategory(placeCategoryLower);

                    // 요청된 카테고리와 실제 카테고리 비교
                    return actualCategory.equals(categoryLower);
                })
                .collect(Collectors.toList());
    }

    /**
     * 장소의 주 카테고리를 우선순위에 따라 결정
     *
     * @param placeCategoryLower 장소 카테고리 (소문자)
     * @return 주 카테고리 ("카페", "문화", "액티비티", "음식점", "기타")
     */
    private String determinePrimaryCategory(String placeCategoryLower) {
        // 우선순위 1: 문화시설
        if (placeCategoryLower.contains("문화")
                || placeCategoryLower.contains("박물관")
                || placeCategoryLower.contains("미술관")
                || placeCategoryLower.contains("갤러리")
                || placeCategoryLower.contains("전시")
                || placeCategoryLower.contains("극장")
                || placeCategoryLower.contains("공연")
                || placeCategoryLower.contains("도서관")
                || placeCategoryLower.contains("library")
                || placeCategoryLower.contains("학습시설")
                || placeCategoryLower.contains("서점")
                || placeCategoryLower.contains("gallery")) {
            return "문화공간";
        }

        // 우선순위 2: 야외활동 (액티비티)
        if (placeCategoryLower.contains("공원")
                || placeCategoryLower.contains("park")
                || placeCategoryLower.contains("산책")
                || placeCategoryLower.contains("야외")
                || placeCategoryLower.contains("outdoor")
                || placeCategoryLower.contains("액티비티")
                || placeCategoryLower.contains("체험")
                || placeCategoryLower.contains("스포츠")
                || placeCategoryLower.contains("운동")
                || placeCategoryLower.contains("레저")
                || placeCategoryLower.contains("오락")
                || placeCategoryLower.contains("게임")
                || placeCategoryLower.contains("볼링")
                || placeCategoryLower.contains("당구")
                || placeCategoryLower.contains("노래방")
                || placeCategoryLower.contains("pc방")
                || placeCategoryLower.contains("activity")
                || placeCategoryLower.contains("sports")
                || placeCategoryLower.contains("관광")     // "여행 > 관광,명소 > ..." 매칭
                || placeCategoryLower.contains("명소")
                || placeCategoryLower.contains("수목원")   // "여행 > 관광,명소 > 수목원,식물원" 매칭
                || placeCategoryLower.contains("식물원")
                || placeCategoryLower.contains("하천")     // "여행 > 관광,명소 > 하천" 매칭
                || placeCategoryLower.contains("산")       // "여행 > 관광,명소 > 산" 매칭
                || placeCategoryLower.contains("계곡")     // "여행 > 관광,명소 > 계곡" 매칭
                || placeCategoryLower.contains("놀이터")   // "가정,생활 > 유아 > 놀이시설 > 놀이터" 매칭
                || placeCategoryLower.contains("놀이시설")
                || placeCategoryLower.contains("여행")) {
            return "액티비티";
        }
        // 우선순위 3: 카페
        if (placeCategoryLower.contains("카페")
                || placeCategoryLower.contains("cafe")
                || placeCategoryLower.contains("디저트")
                || placeCategoryLower.contains("베이커리")
                || placeCategoryLower.contains("bakery")) {
            return "카페";
        }
        // 우선순위 4: 음식점 (카페는 이미 위에서 걸러짐)
        if (placeCategoryLower.contains("음식")
                || placeCategoryLower.contains("식당")
                || placeCategoryLower.contains("음식점")
                || placeCategoryLower.contains("레스토랑")
                || placeCategoryLower.contains("한식")
                || placeCategoryLower.contains("중식")
                || placeCategoryLower.contains("일식")
                || placeCategoryLower.contains("양식")
                || placeCategoryLower.contains("치킨")
                || placeCategoryLower.contains("분식")
                || placeCategoryLower.contains("fastfood")
                || placeCategoryLower.contains("패스트푸드")
                || placeCategoryLower.contains("restaurant")
                || placeCategoryLower.contains("food")) {
            return "음식점";
        }

        // 우선순위 5: 기타 (위 카테고리에 속하지 않는 모든 것)
        return "기타";
    }
}
