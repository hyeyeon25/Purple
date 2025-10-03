package Purple.Purple.itinerery.repository;

import Purple.Purple.itinerery.domain.Itinerary;
import Purple.Purple.user.entity.UserPersonalInfo; // import 변경
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {
    // ★★★ 파라미터 타입을 User -> UserPersonalInfo 로 변경 ★★★
    Optional<Itinerary> findByItineraryIdAndUser(Integer itineraryId, UserPersonalInfo user);
}
