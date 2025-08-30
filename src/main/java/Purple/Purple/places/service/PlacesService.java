package Purple.Purple.places.service;

import Purple.Purple.places.dto.PlaceSummaryDto;
import Purple.Purple.places.dto.PlacesResponseDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class PlacesService {

    /**
     * 동네 내 추천 장소 리스트 조회 (Mock)
     */
    public PlacesResponseDto getPlacesInNeighborhood(int neighborhoodId, String category, String query, String sort, int page, int size, Float centerLat, Float centerLng) {
        // Mock data based on OpenAPI spec
        List<PlaceSummaryDto> mockPlaces = Collections.singletonList(
                PlaceSummaryDto.builder()
                        .locationId(101)
                        .locationName("빈브라더스")
                        .locationCategory("카페")
                        .address("충남 천안시 동남구 유량동 258")
                        .latitude(36.8201f)
                        .longitude(127.1538f)
                        .rating(4.6f)
                        .reviewCount(523)
                        .matchScore(0.87f)
                        .openNow(true)
                        .todayOpen("10:00")
                        .todayClose("20:00")
                        .isIndoor(true)
                        .placeType("조용한")
                        .tags(Arrays.asList("성향:디저트", "분위기:데이트"))
                        .isBookmarked(false)
                        .distance(180)
                        .build()
        );

        // In a real implementation, you would use the parameters (category, query, etc.)
        // to filter, sort, and paginate results from your database.

        return PlacesResponseDto.builder()
                .regionName("천안시")
                .neighborhoodId(neighborhoodId)
                .neighborhoodName("신부동")
                .page(page)
                .size(size)
                .total(42L) // Mock total
                .places(mockPlaces)
                .build();
    }

    /**
     * 장소 상세 정보 조회 (Mock)

    public LocationDetailDto getLocationDetails(int locationId) {
        // Mock data based on OpenAPI spec
        // In a real implementation, you would fetch this from the database.
        // If not found, you would throw an exception (e.g., EntityNotFoundException).
        return LocationDetailDto.builder()
                .locationId(locationId)
                .locationName("빈브라더스")
                .locationCategory("카페")
                .locationAddress("서울특별시 마포구 와우산로...")
                .locationLatitude(37.556f)
                .locationLongitude(126.923f)
                .locationOpenTime("09:00")
                .locationCloseTime("22:00")
                .isIndoor(true)
                .locationSummary("티라미수가 유명한 분위기 좋은 로스터리 카페")
                .locationPlaceType("조용한")
                .liked(true)
                .averageRating(4.6f)
                .reviewCount(23)
                .build();
    }
     */
}