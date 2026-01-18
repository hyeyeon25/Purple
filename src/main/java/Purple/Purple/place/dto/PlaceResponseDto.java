package Purple.Purple.place.dto;

import Purple.Purple.place.entity.PlaceEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class PlaceResponseDto {

    private final Integer placeId;
    private final String placeName;
    private final String placeCategory;
    private final String address;
    private final Double latitude;
    private final Double longitude;
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
                .recommendedSlot(entity.getRecommendedSlot() != null ? RecommendedSlot.valueOf(entity.getRecommendedSlot()) : null)
                .build();
    }
}