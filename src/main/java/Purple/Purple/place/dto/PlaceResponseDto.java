package Purple.Purple.place.dto;

import Purple.Purple.place.entity.PlaceEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

/**
 * - Service 계층이 처리 결과를 Controller에 반환할 때 사용
 * - Entity의 모든 필드를 노출하지 않고, 클라이언트에 필요한 정보만 선별하여 제공합니다.
 */
@Getter
@Builder
public class PlaceResponseDto {

    private final Integer placeId;
    private final String placeName;
    private final String placeCategory;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final RecommendedSlot recommendedSlot;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static PlaceResponseDto from(PlaceEntity entity) {
        return PlaceResponseDto.builder()
                .placeId(entity.getPlaceId())
                .placeName(entity.getPlaceName())
                .placeCategory(entity.getPlaceCategory())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .openTime(entity.getOpenTime())
                .closeTime(entity.getCloseTime())
                .recommendedSlot(entity.getRecommendedSlot() != null ? RecommendedSlot.valueOf(entity.getRecommendedSlot()) : null)
                .build();
    }
}