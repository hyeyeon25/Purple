package Purple.Purple.place.repository;

import Purple.Purple.Neighborhood.entity.NeighborhoodEntity;
import Purple.Purple.place.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceEntity, Integer> {

    /**
     * 카카오 장소 ID로 장소 엔티티를 조회
     *
     * @param kakaoPlaceId 카카오에서 발급한 고유 장소 ID
     * @return Optional<PlaceEntity> (결과가 없을 수도 있으므로 Optional로 감싸서 반환)
     */
    Optional<PlaceEntity> findByKakaoPlaceId(String kakaoPlaceId);

    /**
     * 특정 동네에 속한 모든 장소를 조회
     *
     * @param neighborhood 동네 엔티티
     * @return 해당 동네의 장소 리스트
     */
    List<PlaceEntity> findByNeighborhood(NeighborhoodEntity neighborhood);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaceEntity p SET p.summary = :summary WHERE p.placeId = :placeId")
    void updatePlaceSummary(@Param("placeId") Integer placeId, @Param("summary") String summary);

}