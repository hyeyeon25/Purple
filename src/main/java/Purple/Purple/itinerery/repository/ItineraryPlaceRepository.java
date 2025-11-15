package Purple.Purple.itinerery.repository;

import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.itinerery.domain.ItineraryPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItineraryPlaceRepository extends JpaRepository<ItineraryPlace, Integer> {
	List<ItineraryPlace> findByItinerary(Itinerary itinerary);
}
