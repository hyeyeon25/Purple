package Purple.Purple.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

// 여행 계획(폴더) 생성을 위한 요청 DTO
@Getter
@NoArgsConstructor
public class ItineraryCreateRequestDto {
    private String itineraryTitle;
    private LocalDate date;
}
