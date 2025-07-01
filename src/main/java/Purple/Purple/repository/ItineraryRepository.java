package Purple.Purple.repository;

import Purple.Purple.domain.Itinerary;
import Purple.Purple.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {
    // 사용자가 소유한 모든 여행 계획(폴더) 조회
    List<Itinerary> findByUser(User user);

    // 특정 사용자가 소유한 특정 여행 계획 조회 (소유권 검증용)
    Optional<Itinerary> findByItineraryIdAndUser(Integer itineraryId, User user);
}
