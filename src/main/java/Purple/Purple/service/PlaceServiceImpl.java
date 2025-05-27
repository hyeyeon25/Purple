package Purple.Purple.service;

import Purple.Purple.domian.Place;
import Purple.Purple.dto.PlaceRequest;
import Purple.Purple.dto.PlaceResponse;
import Purple.Purple.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceServiceImpl implements PlaceService {

    @Autowired
    private PlaceRepository placeRepository;

    @Override
    public PlaceResponse create(PlaceRequest request) {
        Place place = new Place();
        place.setName(request.name);
        place.setDescription(request.description);
        place.setCategory(request.category);
        place.setLocation(request.location);
        place.setRecommended(request.recommended);
        place = placeRepository.save(place);
        return toResponse(place);
    }

    @Override
    public PlaceResponse get(Long id) {
        return placeRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public List<PlaceResponse> getAll() {
        return placeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PlaceResponse update(Long id, PlaceRequest request) {
        return placeRepository.findById(id).map(place -> {
            place.setName(request.name);
            place.setDescription(request.description);
            place.setCategory(request.category);
            place.setLocation(request.location);
            place.setRecommended(request.recommended);
            return toResponse(placeRepository.save(place));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        placeRepository.deleteById(id);
    }

    private PlaceResponse toResponse(Place place) {
        PlaceResponse res = new PlaceResponse();
        res.id = place.getId();
        res.name = place.getName();
        res.description = place.getDescription();
        res.category = place.getCategory();
        res.location = place.getLocation();
        res.recommended = place.isRecommended();
        return res;
    }
}
