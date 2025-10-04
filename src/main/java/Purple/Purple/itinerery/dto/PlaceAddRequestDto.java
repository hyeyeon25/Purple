package Purple.Purple.itinerery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

// 여행 계획에 장소를 추가하기 위한 요청 DTO
@Getter
@NoArgsConstructor
public class PlaceAddRequestDto {
    private Integer placeId;
}
