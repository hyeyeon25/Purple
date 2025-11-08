package Purple.Purple.Neighborhood.service;

import Purple.Purple.Neighborhood.dto.NeighborhoodRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.PlaceRecommendationResponseDto;
import Purple.Purple.Neighborhood.dto.UserPreferenceRequestDto;

import java.util.List;

/**
 * 동네 추천 서비스 인터페이스
 */
public interface NeighborhoodRecommendationService {

    /**
     * 사용자 ID 기반 동네 Top 3 추천
     * 사용자의 저장된 선호도 벡터를 자동으로 조회하여 추천합니다.
     *
     * @param userId 사용자 ID
     * @return 추천 동네 Top 3 리스트 (유사도 점수 높은 순)
     */
    List<NeighborhoodRecommendationResponseDto> recommendTop3NeighborhoodsByUserId(Long userId);

    /**
     * 사용자 ID 기반 특정 동네 내 장소 추천 (카테고리 필터링)
     * 사용자의 저장된 선호도 벡터를 자동으로 조회하여 추천합니다.
     *
     * @param neighborhoodId 동네 ID
     * @param category 필터링할 카테고리 ("음식점", "카페", "문화", "액티비티", "기타")
     * @param userId 사용자 ID
     * @return 추천 장소 리스트 (유사도 점수 높은 순)
     */
    List<PlaceRecommendationResponseDto> recommendPlacesInNeighborhoodByUserId(
            Integer neighborhoodId,
            String category,
            Long userId);
}
