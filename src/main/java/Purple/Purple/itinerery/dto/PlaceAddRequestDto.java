package Purple.Purple.itinerery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 여행 계획에 장소를 추가하기 위한 요청 DTO
@Getter
@NoArgsConstructor
public class PlaceAddRequestDto {
    private Integer placeId; // 기존 장소 ID (있으면 사용)
    private String kakaoPlaceId; // 카카오 장소 ID (중복 체크용)
    private String placeName;
    private String placeCategory;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer neighborhoodId;
}
