package Purple.Purple.place.repository;

import Purple.Purple.place.entity.PlaceAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaceAnalysisRepository extends JpaRepository<PlaceAnalysisEntity, Long> {
  // 장소 ID(PK)로 분석 데이터 찾기
  Optional<PlaceAnalysisEntity> findByPlace_PlaceId(Integer placeId);
}
