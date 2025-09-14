package Purple.Purple.user.repository;

import Purple.Purple.user.entity.RefreshEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Boolean existsByRefresh(String refresh); // 이름으로 조회했을때 리프레쉬 토큰 존재하는가..!

    //void deleteByRefresh(String refresh);//db내에 리프레쉬토큰 지울때..!
    @Transactional              // 메서드 실행 시 트랜잭션 시작
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int deleteByRefresh(String refresh);
}
