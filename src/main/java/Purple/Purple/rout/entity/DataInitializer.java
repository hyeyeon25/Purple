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
                    Location.builder()
                            .name("천안종합버스터미널")
                            .latitude(36.8219)
                            .longitude(127.1576)
                            .address("충남 천안시 동남구 만남로 43")
                            .phone("041-561-0000")
                            .aiSummary("천안 주요 버스터미널로 접근성과 편의시설이 좋은 교통 중심지예요.")
                            .build(),

                    Location.builder()
                            .name("천안아산역")
                            .latitude(36.7961)
                            .longitude(127.1029)
                            .address("충남 아산시 배방읍 희망로 100")
                            .phone("1544-7788")
                            .aiSummary("KTX·SRT 환승이 편리한 천안·아산권 대표 철도역이에요.")
                            .build(),

                    Location.builder()
                            .name("천안삼거리공원")
                            .latitude(36.8017)
                            .longitude(127.1530)
                            .address("충남 천안시 동남구 삼룡동 306")
                            .phone("041-521-0000")
                            .aiSummary("산책하기 좋은 천안 대표 공원으로 계절마다 다양한 문화행사가 열려요.")
                            .build(),

                    Location.builder()
                            .name("독립기념관")
                            .latitude(36.7796)
                            .longitude(127.1527)
                            .address("충남 천안시 동남구 목천읍 독립기념관로 1")
                            .phone("041-560-0114")
                            .aiSummary("한국 근현대사의 독립운동을 한눈에 볼 수 있는 대규모 전시·체험 공간이에요.")
                            .build(),

                    Location.builder()
                            .name("아라리오갤러리 천안")
                            .latitude(36.8156)
                            .longitude(127.1526)
                            .address("충남 천안시 동남구 만남로 43")
                            .phone("041-551-5100")
                            .aiSummary("현대미술 전시가 열리는 감각적인 갤러리로 사진 명소로도 유명해요.")
                            .build()
            );
            locationRepository.saveAll(dummyLocations);
            log.info("데이터 저장 성공");

        }
    }
}
