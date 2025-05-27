package Purple.Purple.service;

import Purple.Purple.dto.PlaceRequest;
import Purple.Purple.dto.PlaceResponse;

import java.util.List;

public interface PlaceService {
    PlaceResponse create(PlaceRequest request);
    PlaceResponse get(Long id);
    List<PlaceResponse> getAll();
    PlaceResponse update(Long id, PlaceRequest request);
    void delete(Long id);
}
