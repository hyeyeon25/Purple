package Purple.Purple.preferences.repository;

import Purple.Purple.preferences.entity.PreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreferencesRepository extends JpaRepository<PreferencesEntity, Long> {
    Optional<PreferencesEntity> findByUserid(Integer userid);
    boolean existsByUserid(Integer userid);
}
