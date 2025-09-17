package Purple.Purple.Neighborhood.repository;


import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NeighborhoodRepository extends JpaRepository<NeighborhoodEntity, Integer> {
}