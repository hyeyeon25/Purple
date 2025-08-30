package Purple.Purple.recommendations.service;

import Purple.Purple.neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.neighborhood.repository.NeighborhoodRepository;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import Purple.Purple.recommendations.dto.BestNeighborhoodDto;
import Purple.Purple.recommendations.dto.RecommendedNeighborhoodDto;
import Purple.Purple.recommendations.dto.TopNeighborhoodsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationsService {

    private final PreferencesRepository preferencesRepository;
    private final NeighborhoodRepository neighborhoodRepository;

    @Transactional(readOnly = true)
    public TopNeighborhoodsResponseDto getTop3Recommendations(String region, int userId) {

        // 1. 실제 DB에서 사용자 성향 정보 조회
        PreferencesEntity userPreferences = preferencesRepository.findByUserid(userId)
                .orElseThrow(() -> new IllegalArgumentException("ID " + userId + "에 해당하는 사용자 성향 정보가 없습니다."));

        // 2. 해당 지역의 동네 및 장소 정보 조회
        List<NeighborhoodEntity> neighborhoods = neighborhoodRepository.findAllByRegionNameWithPlaces(region);
        if (neighborhoods.isEmpty()) {
            throw new RuntimeException(region + " 지역의 동네 정보를 찾을 수 없습니다.");
        }

        // 3. 각 동네별 점수 계산 후 정렬하여 Top 3 반환
        List<RecommendedNeighborhoodDto> scoredNeighborhoods = neighborhoods.stream()
                .map(neighborhood -> {
                    float score = calculateMatchScore(userPreferences, neighborhood);
                    return RecommendedNeighborhoodDto.builder()
                            .neighborhoodId(neighborhood.getId())
                            .neighborhoodName(neighborhood.getName())
                            .neighborhoodLatitude(neighborhood.getLatitude())
                            .neighborhoodLongitude(neighborhood.getLongitude())
                            .matchScore(score)
                            .build();
                })
                .sorted(Comparator.comparing(RecommendedNeighborhoodDto::getMatchScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        return TopNeighborhoodsResponseDto.builder()
                .regionName(region)
                .recommendedNeighborhoods(scoredNeighborhoods)
                .build();
    }

    public BestNeighborhoodDto getBestRecommendation(String region, int userId) {
        TopNeighborhoodsResponseDto top3Response = getTop3Recommendations(region, userId);

        if (top3Response.getRecommendedNeighborhoods().isEmpty()) {
            throw new RuntimeException("추천할 동네를 찾을 수 없습니다.");
        }
        RecommendedNeighborhoodDto bestNeighborhood = top3Response.getRecommendedNeighborhoods().get(0);

        return BestNeighborhoodDto.builder()
                .neighborhoodId(bestNeighborhood.getNeighborhoodId())
                .neighborhoodName(bestNeighborhood.getNeighborhoodName())
                .matchScore(bestNeighborhood.getMatchScore())
                .heroImageUrl("https://cdn.example.com/hero/nbd-" + bestNeighborhood.getNeighborhoodId() + ".jpg")
                .subImages(List.of(
                        "https://cdn.example.com/hero/nbd-" + bestNeighborhood.getNeighborhoodId() + "-sub1.jpg",
                        "https://cdn.example.com/hero/nbd-" + bestNeighborhood.getNeighborhoodId() + "-sub2.jpg"
                ))
                .build();
    }

    /**
     * 동네 추천 알고리즘
     * 사용자 성향과 동네의 장소 특성을 비교하여 매칭 점수를 계산합니다. (0 ~ 100점)
     * 모든 점수에 성향 반영
     */
    private float calculateMatchScore(PreferencesEntity prefs, NeighborhoodEntity neighborhood) {
        // List<PlaceEntity> places = neighborhood.getPlaces();

        // --- 평가 항목 1: 실내/실외 선호도 (40점 만점) ---

        // --- 평가 항목 2: 외향/내향 선호도 (30점 만점) ---

        // --- 평가 항목 3: 문화 활동 선호도 (30점 만점) ---
        // 사용자의 문화 선호도(0~100)와 동네의 문화공간 비율을 곱하여 점수 산정
        // TODO: 음식(foodPreference), 디저트(desertPreference) 성향도 위와 같은 방식으로 추가 가능

        return Math.min(100.0f, 100); // 최종 점수가 100점을 넘지 않도록 보정해서 넌ㅁ겨주기
    }
}