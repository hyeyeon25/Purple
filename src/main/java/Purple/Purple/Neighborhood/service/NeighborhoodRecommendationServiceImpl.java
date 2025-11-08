package Purple.Purple.Neighborhood.service;

import Purple.Purple.Neighborhood.dto.NeighborhoodRecommendationResponseDto;
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

    // 고정된 태그 사전 (TagVectorizationServiceImpl과 동일)
    private static final List<String> TAG_DICTIONARY = Arrays.asList(
            "음식점", "카페", "액티비티", "문화",
            "실내", "실외",
            "아침추천", "점심추천", "오후추천", "저녁추천", "밤추천",
            "데이트", "가족", "친구", "혼자"
    );

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
    public List<PlaceRecommendationResponseDto> recommendPlacesInNeighborhoodByUserId(
            Integer neighborhoodId,
            String category,
            Long userId) {

        log.info("Starting place recommendation for userId: {}, neighborhoodId: {}, category: {}",
                userId, neighborhoodId, category);

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

        // 3. 장소 추천 로직 실행
        return recommendPlacesWithVector(neighborhoodId, category, normalizedUserVector);
    }

    /**
     * 벡터 기반 동네 추천 (내부 로직)
     */
    private List<NeighborhoodRecommendationResponseDto> recommendNeighborhoodsWithVector(List<Double> normalizedUserVector) {
        log.debug("User preference vector (normalized): {}", normalizedUserVector);

        // 2. 모든 동네 조회
        List<NeighborhoodEntity> allNeighborhoods = neighborhoodRepository.findAll();
        List<NeighborhoodRecommendationResponseDto> recommendations = new ArrayList<>();

        // 3. 각 동네별 평균 유사도 점수 계산
        for (NeighborhoodEntity neighborhood : allNeighborhoods) {
            // 해당 동네의 모든 장소 조회
            List<PlaceEntity> places = placeRepository.findByNeighborhood(neighborhood);

            if (places.isEmpty()) {
                log.debug("Neighborhood {} has no places, skipping", neighborhood.getNeighborhoodName());
                continue;
            }

            // 각 장소와의 코사인 유사도 계산
            List<Double> similarityScores = new ArrayList<>();
            for (PlaceEntity place : places) {
                if (place.getTagVector() == null || place.getTagVector().isEmpty()) {
                    log.debug("Place {} has no tag vector, skipping", place.getPlaceName());
                    continue;
                }

                try {
                    List<Double> placeVector = parseVectorFromJson(place.getTagVector());
                    double similarity = calculateCosineSimilarity(normalizedUserVector, placeVector);
                    similarityScores.add(similarity);
                } catch (Exception e) {
                    log.warn("Failed to parse vector for place {}: {}", place.getPlaceName(), e.getMessage());
                }
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

        // 4. 평균 유사도 점수 기준 내림차순 정렬 후 Top 3 선택
        List<NeighborhoodRecommendationResponseDto> top3 = recommendations.stream()
                .sorted(Comparator.comparing(NeighborhoodRecommendationResponseDto::getAverageSimilarityScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 5. 순위 부여
        for (int i = 0; i < top3.size(); i++) {
            top3.get(i).setRank(i + 1);
        }

        log.info("Top 3 neighborhoods recommended: {}",
                top3.stream()
                        .map(NeighborhoodRecommendationResponseDto::getNeighborhoodName)
                        .collect(Collectors.toList()));

        return top3;
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
     * TagVectorizationServiceImpl의 generateVector와 동일한 로직
     */
    private List<Double> generateVector(List<String> tags) {
        List<Double> vector = new ArrayList<>();

        for (String dictTag : TAG_DICTIONARY) {
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
     * 벡터 기반 장소 추천 (내부 로직)
     */
    private List<PlaceRecommendationResponseDto> recommendPlacesWithVector(
            Integer neighborhoodId,
            String category,
            List<Double> normalizedUserVector) {

        log.info("Starting place recommendation for neighborhood ID: {}, category: {}",
                neighborhoodId, category);

        // 1. 동네 조회
        NeighborhoodEntity neighborhood = neighborhoodRepository.findById(neighborhoodId)
                .orElseThrow(() -> new IllegalArgumentException("동네를 찾을 수 없습니다. ID: " + neighborhoodId));

        // 2. 해당 동네의 모든 장소 조회
        List<PlaceEntity> places = placeRepository.findByNeighborhood(neighborhood);

        if (places.isEmpty()) {
            log.warn("No places found in neighborhood: {}", neighborhood.getNeighborhoodName());
            return Collections.emptyList();
        }

        // 3. 카테고리로 필터링
        List<PlaceEntity> filteredPlaces = filterPlacesByCategory(places, category);

        if (filteredPlaces.isEmpty()) {
            log.warn("No places found matching category: {} in neighborhood: {}",
                    category, neighborhood.getNeighborhoodName());
            return Collections.emptyList();
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
                        .build();

                recommendations.add(dto);

            } catch (Exception e) {
                log.warn("Failed to process place {}: {}", place.getPlaceName(), e.getMessage());
            }
        }

        // 6. 유사도 점수 기준 내림차순 정렬
        List<PlaceRecommendationResponseDto> sortedRecommendations = recommendations.stream()
                .sorted(Comparator.comparing(PlaceRecommendationResponseDto::getSimilarityScore).reversed())
                .collect(Collectors.toList());

        log.info("Recommended {} places in neighborhood {} (category: {})",
                sortedRecommendations.size(), neighborhood.getNeighborhoodName(), category);

        return sortedRecommendations;
    }

    /**
     * 카테고리로 장소 필터링
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

                    // 카테고리 매칭 로직
                    switch (categoryLower) {
                        case "음식점":
                            return placeCategoryLower.contains("음식")
                                    || placeCategoryLower.contains("식당")
                                    || placeCategoryLower.contains("레스토랑")
                                    || placeCategoryLower.contains("한식")
                                    || placeCategoryLower.contains("중식")
                                    || placeCategoryLower.contains("일식")
                                    || placeCategoryLower.contains("양식")
                                    || placeCategoryLower.contains("치킨")
                                    || placeCategoryLower.contains("분식")
                                    || placeCategoryLower.contains("fast food")
                                    || placeCategoryLower.contains("패스트푸드");

                        case "카페":
                            return placeCategoryLower.contains("카페")
                                    || placeCategoryLower.contains("cafe")
                                    || placeCategoryLower.contains("디저트")
                                    || placeCategoryLower.contains("베이커리");

                        case "문화":
                            return placeCategoryLower.contains("문화")
                                    || placeCategoryLower.contains("박물관")
                                    || placeCategoryLower.contains("미술관")
                                    || placeCategoryLower.contains("갤러리")
                                    || placeCategoryLower.contains("전시")
                                    || placeCategoryLower.contains("극장")
                                    || placeCategoryLower.contains("공연");

                        case "액티비티":
                            return placeCategoryLower.contains("액티비티")
                                    || placeCategoryLower.contains("체험")
                                    || placeCategoryLower.contains("스포츠")
                                    || placeCategoryLower.contains("운동")
                                    || placeCategoryLower.contains("레저")
                                    || placeCategoryLower.contains("오락")
                                    || placeCategoryLower.contains("게임")
                                    || placeCategoryLower.contains("볼링")
                                    || placeCategoryLower.contains("당구")
                                    || placeCategoryLower.contains("노래방")
                                    || placeCategoryLower.contains("pc방");

                        case "기타":
                            // 음식점, 카페, 문화, 액티비티에 해당하지 않는 모든 장소
                            return !(placeCategoryLower.contains("음식")
                                    || placeCategoryLower.contains("식당")
                                    || placeCategoryLower.contains("레스토랑")
                                    || placeCategoryLower.contains("카페")
                                    || placeCategoryLower.contains("cafe")
                                    || placeCategoryLower.contains("문화")
                                    || placeCategoryLower.contains("박물관")
                                    || placeCategoryLower.contains("미술관")
                                    || placeCategoryLower.contains("액티비티")
                                    || placeCategoryLower.contains("체험"));

                        default:
                            // 알 수 없는 카테고리는 모든 장소 반환
                            log.warn("Unknown category: {}", category);
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }
}
