package Purple.Purple.place.service;

import Purple.Purple.place.dto.PlaceResponseDto;

import Purple.Purple.place.entity.PlaceAnalysisEntity;
import java.util.List;

public interface PlaceService {

    void fetchAllPlacesForCheonan();
    PlaceResponseDto updatePlace(Integer placeId);

    PlaceAnalysisEntity getAnalysisData(Integer dbPlaceId, String naverPlaceId);
}