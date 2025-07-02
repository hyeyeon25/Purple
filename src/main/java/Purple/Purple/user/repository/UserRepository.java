package Purple.Purple.user.repository;

import Purple.Purple.user.entity.UserPersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserPersonalInfo, Long> {
    Optional<UserPersonalInfo> findByEmail(String email);
    Optional<UserPersonalInfo> findByNickname(String nickname);
}
