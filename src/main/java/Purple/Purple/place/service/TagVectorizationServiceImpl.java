package Purple.Purple.place.service;

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

    // 고정된 태그 사전 (원-핫 인코딩용)
    private static final List<String> TAG_DICTIONARY = Arrays.asList(
            "음식점", "카페", "액티비티", "문화",
            "실내", "실외",
            "아침추천", "점심추천", "오후추천", "저녁추천", "밤추천",
            "데이트", "가족", "친구", "혼자"
    );

    @Override
    @Transactional
    public TagVectorizationResponseDto vectorizeSinglePlace(Integer placeId, TagVectorizationRequestDto requestDto) {
        log.info("Starting vectorization for place ID: {}", placeId);

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
     * PlaceEntity로부터 태그 추출
     */
    private List<String> extractTagsFromPlace(PlaceEntity place) {
        List<String> tags = new ArrayList<>();

        // 카테고리 기반 태그
        if (place.getScoreFood() != null && place.getScoreFood() > 0) {
            tags.add("음식점");
        }
        if (place.getScoreCafe() != null && place.getScoreCafe() > 0) {
            tags.add("카페");
        }
        if (place.getScoreActivity() != null && place.getScoreActivity() > 0) {
            tags.add("액티비티");
        }
        if (place.getScoreCulture() != null && place.getScoreCulture() > 0) {
            tags.add("문화");
        }

        // 실내/실외
        if (place.getIsIndoor() != null) {
            tags.add(place.getIsIndoor() ? "실내" : "실외");
        }

        // 추천 시간대
        if (place.getRecommendedSlot() != null) {
            switch (place.getRecommendedSlot().toUpperCase()) {
                case "BREAKFAST":
                    tags.add("아침추천");
                    break;
                case "LUNCH":
                    tags.add("점심추천");
                    break;
                case "AFTERNOON":
                    tags.add("오후추천");
                    break;
                case "DINNER":
                    tags.add("저녁추천");
                    break;
                case "NIGHT":
                    tags.add("밤추천");
                    break;
            }
        }

        // 카테고리 키워드 분석
        if (place.getPlaceCategory() != null) {
            String category = place.getPlaceCategory().toLowerCase();
            if (category.contains("카페") || category.contains("cafe")) {
                tags.add("카페");
            }
            if (category.contains("음식") || category.contains("식당") || category.contains("레스토랑")) {
                tags.add("음식점");
            }
        }

        return tags.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 원-핫 인코딩으로 벡터 생성
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