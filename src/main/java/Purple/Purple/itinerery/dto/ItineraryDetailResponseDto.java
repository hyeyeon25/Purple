package Purple.Purple.itinerery.dto;

import Purple.Purple.itinerery.domain.Itinerary;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// 여행 계획(폴더)의 상세 정보를 담는 응답 DTO
@Getter
@NoArgsConstructor
public class ItineraryDetailResponseDto {
    private Integer itineraryId;
    private String itineraryTitle;
    private LocalDate date;
    private List<ItineraryPlaceResponseDto> places;

    public ItineraryDetailResponseDto(Itinerary itinerary) {
        this.itineraryId = itinerary.getItineraryId();
        this.itineraryTitle = itinerary.getItineraryTitle();
        this.date = itinerary.getDate();
        this.places = itinerary.getItineraryPlaces().stream()
                .map(ItineraryPlaceResponseDto::new)
                .collect(Collectors.toList());
    }
}
