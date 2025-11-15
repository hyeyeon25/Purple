package Purple.Purple.itinerery.repository;

import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.folder.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {
	Optional<Itinerary> findByFolder(Folder folder);
}
