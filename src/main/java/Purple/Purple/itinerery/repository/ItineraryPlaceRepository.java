package Purple.Purple.itinerery.repository;

import Purple.Purple.itinerery.domain.ItineraryPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryPlaceRepository extends JpaRepository<ItineraryPlace, Integer> {
}
