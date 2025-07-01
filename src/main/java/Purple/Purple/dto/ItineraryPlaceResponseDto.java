package Purple.Purple.dto;

import Purple.Purple.domain.ItineraryPlace;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 여행 계획 내의 각 장소 정보를 담는 응답 DTO
@Getter
@NoArgsConstructor
public class ItineraryPlaceResponseDto {
    private Integer itineraryPlaceId;
    private Integer placeId;
    private String placeName;
    private String address;
    private Integer visitOrder;
    private String memo;

    public ItineraryPlaceResponseDto(ItineraryPlace itineraryPlace) {
        this.itineraryPlaceId = itineraryPlace.getItineraryPlaceId();
        this.placeId = itineraryPlace.getPlace().getPlaceId();
        this.placeName = itineraryPlace.getPlace().getPlaceName();
        this.address = itineraryPlace.getPlace().getAddress();
        this.visitOrder = itineraryPlace.getVisitOrder();
        this.memo = itineraryPlace.getMemo();
    }
}
