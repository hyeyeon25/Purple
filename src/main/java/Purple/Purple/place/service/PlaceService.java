package Purple.Purple.place.service;

import Purple.Purple.place.dto.PlaceResponseDto;

public interface PlaceService {

    void fetchAllPlacesForCheonan();
    PlaceResponseDto updatePlace(Integer placeId);
}