package Purple.Purple.rout.entity;

import Purple.Purple.rout.entity.Location;
import Purple.Purple.rout.repository.LocationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final LocationRepository locationRepository;
    @PostConstruct
    // Spring의 @PostConstruct를 사용해 초기화
    public void init() {
        if (locationRepository.count() == 0) { // 중복 저장 방지
            List<Location> dummyLocations = List.of(
                    new Location(null, "천안종합버스터미널", 36.8219, 127.1576),
                    new Location(null, "천안아산역", 36.7961, 127.1029),
                    new Location(null, "천안삼거리공원", 36.8017, 127.1530),
                    new Location(null, "독립기념관", 36.7796, 127.1527),
                    new Location(null, "아라리오갤러리 천안", 36.8156, 127.1526)
            );
            locationRepository.saveAll(dummyLocations);
            log.info("데이터 저장 성공");

        }
    }
}
