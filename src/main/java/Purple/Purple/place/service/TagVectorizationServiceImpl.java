package Purple.Purple.place.service;

import Purple.Purple.common.constants.TagDictionary;
import Purple.Purple.place.dto.BatchVectorizationResponseDto;
import Purple.Purple.place.dto.TagVectorizationRequestDto;
import Purple.Purple.place.dto.TagVectorizationResponseDto;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.place.repository.PlaceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class TagVectorizationServiceImpl implements TagVectorizationService {

    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TagVectorizationResponseDto vectorizeSinglePlace(Integer placeId, TagVectorizationRequestDto requestDto) {

        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다. ID: " + placeId));

        try {
            // 1. 태그 추출
            List<String> tags = (requestDto != null && requestDto.getTags() != null && !requestDto.getTags().isEmpty())
                    ? requestDto.getTags()
                    : extractTagsFromPlace(place);

            // 2. 기존 태그가 있고 덮어쓰기가 false면 기존 태그 유지
            if (place.getTags() != null && !place.getTags().isEmpty()
                    && (requestDto == null || !requestDto.getOverwrite())) {
                List<String> existingTags = Arrays.asList(place.getTags().split(","));
                tags = new ArrayList<>(new HashSet<>(tags)); // 중복 제거
                tags.addAll(existingTags);
                tags = tags.stream().distinct().collect(Collectors.toList());
            }

            // 3. 벡터 생성
            List<Double> vector = generateVector(tags);

            // 4. 정규화
            List<Double> normalizedVector = normalizeVector(vector);

            // 5. DB 저장
            place.setTags(String.join(",", tags));
            place.setTagVector(convertVectorToJson(normalizedVector));
            placeRepository.save(place);

            log.info("Vectorization completed for place ID: {}. Tags: {}, Vector dimension: {}",
                    placeId, tags, normalizedVector.size());

            return TagVectorizationResponseDto.builder()
                    .placeId(place.getPlaceId())
                    .placeName(place.getPlaceName())
                    .tags(tags)
                    .vector(normalizedVector)
                    .vectorDimension(normalizedVector.size())
                    .success(true)
                    .message("벡터화가 완료되었습니다.")
                    .build();

        } catch (Exception e) {
            log.error("Error during vectorization for place ID: {}", placeId, e);
            return TagVectorizationResponseDto.builder()
                    .placeId(placeId)
                    .success(false)
                    .message("벡터화 실패: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public BatchVectorizationResponseDto vectorizeAllPlaces() {
        log.info("Starting batch vectorization for all places");
        long startTime = System.currentTimeMillis();

        List<PlaceEntity> allPlaces = placeRepository.findAll();
        int totalPlaces = allPlaces.size();
        int successCount = 0;
        int failureCount = 0;

        for (PlaceEntity place : allPlaces) {
            try {
                TagVectorizationRequestDto requestDto = TagVectorizationRequestDto.builder()
                        .overwrite(false)
                        .build();
                TagVectorizationResponseDto result = vectorizeSinglePlace(place.getPlaceId(), requestDto);

                if (result.getSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                log.error("Failed to vectorize place ID: {}", place.getPlaceId(), e);
                failureCount++;
            }
        }

        long endTime = System.currentTimeMillis();
        double processingTime = (endTime - startTime) / 1000.0;

        log.info("Batch vectorization completed. Total: {}, Success: {}, Failure: {}, Time: {}s",
                totalPlaces, successCount, failureCount, processingTime);

        return BatchVectorizationResponseDto.builder()
                .totalPlaces(totalPlaces)
                .successCount(successCount)
                .failureCount(failureCount)
                .processingTimeSeconds(processingTime)
                .message(String.format("배치 벡터화가 완료되었습니다. (성공: %d, 실패: %d)", successCount, failureCount))
                .build();
    }

    /**
     * PlaceEntity로부터 태그 추출 (56개 세분화된 태그 사전 기반)
     */
    private List<String> extractTagsFromPlace(PlaceEntity place) {
        List<String> tags = new ArrayList<>();

        // 실내/실외
        if (place.getIsIndoor() != null) {
            tags.add(place.getIsIndoor() ? "실내" : "실외");
        }

        // 추천 시간대 (세분화)
        if (place.getRecommendedSlot() != null) {
            switch (place.getRecommendedSlot().toUpperCase()) {
                case "BREAKFAST":
                    tags.add("아침");
                    break;
                case "LUNCH":
                    tags.add("점심");
                    break;
                case "AFTERNOON":
                    tags.add("오후");
                    break;
                case "DINNER":
                    tags.add("저녁");
                    break;
                case "NIGHT":
                    tags.add("밤");
                    break;
            }
        }

        // 카테고리 키워드 분석 (세분화)
        if (place.getPlaceCategory() != null) {
            String category = place.getPlaceCategory().toLowerCase();

            // 음식 카테고리
            if (category.contains("한식") || category.contains("korean")) {
                tags.add("한식");
            }
            if (category.contains("양식") || category.contains("western") || category.contains("이탈리안") ||
                category.contains("프랑스") || category.contains("스테이크") || category.contains("파스타")) {
                tags.add("양식");
            }
            if (category.contains("아시안") || category.contains("asian") || category.contains("태국") ||
                category.contains("베트남") || category.contains("인도") ||
                category.contains("일식") || category.contains("일본") || category.contains("japanese") ||
                category.contains("스시") || category.contains("라멘") ||
                category.contains("중식") || category.contains("중국") || category.contains("chinese")) {
                tags.add("아시안");
            }
            if (category.contains("퓨전") || category.contains("fusion") || category.contains("이색")) {
                tags.add("이색/퓨전");
            }
            if (category.contains("분식") || category.contains("떡볶이") || category.contains("김밥")) {
                tags.add("분식");
            }
            if (category.contains("건강식") || category.contains("샐러드") || category.contains("salad") ||
                category.contains("비건") || category.contains("vegan") || category.contains("채식") ||
                category.contains("헬시") || category.contains("healthy")) {
                tags.add("건강식");
            }

            // 음료/디저트
            if (category.contains("카페") || category.contains("cafe") || category.contains("커피")) {
                tags.add("카페");
            }
            if (category.contains("디저트") || category.contains("dessert") || category.contains("케이크")) {
                tags.add("디저트");
            }
            if (category.contains("베이커리") || category.contains("bakery") || category.contains("빵")) {
                tags.add("베이커리");
            }
            if (category.contains("술집") || category.contains("이자카야") || category.contains("선술집")) {
                tags.add("술집");
            }
            if (category.contains("바") || category.contains("bar") || category.contains("펜")) {
                tags.add("바");
            }
            if (category.contains("주류") || category.contains("와인") || category.contains("맥주")) {
                tags.add("주류");
            }

            // 액티비티/문화
            if (category.contains("액티비티") || category.contains("activity")) {
                tags.add("액티비티");
            }
            if (category.contains("문화") || category.contains("culture")) {
                tags.add("문화생활");
            }
            if (category.contains("스포츠") || category.contains("sport") || category.contains("운동")) {
                tags.add("스포츠");
            }
            if (category.contains("게임") || category.contains("game") || category.contains("오락")) {
                tags.add("게임");
            }
            if (category.contains("체험") || category.contains("experience")) {
                tags.add("체험");
            }
            if (category.contains("전시") || category.contains("exhibition") || category.contains("갤러리")) {
                tags.add("전시");
            }
            if (category.contains("공연") || category.contains("performance") || category.contains("극장")) {
                tags.add("공연");
            }
            if (category.contains("영화") || category.contains("cinema") || category.contains("theater")) {
                tags.add("영화관");
            }
            if (category.contains("미술관") || category.contains("art museum")) {
                tags.add("미술관");
            }
            if (category.contains("박물관") || category.contains("museum")) {
                tags.add("박물관");
            }
            if (category.contains("서점") || category.contains("bookstore") || category.contains("책방")) {
                tags.add("서점");
            }

            // 기타
            if (category.contains("쇼핑") || category.contains("shopping") || category.contains("마트")) {
                tags.add("쇼핑");
            }
            if (category.contains("공원") || category.contains("park")) {
                tags.add("공원");
            }
            if (category.contains("산책") || category.contains("walk")) {
                tags.add("산책");
            }
        }

        return tags.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
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
     * 벡터를 JSON 문자열로 변환
     */
    private String convertVectorToJson(List<Double> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert vector to JSON", e);
            return "[]";
        }
    }
}