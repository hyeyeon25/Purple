package Purple.Purple.config;

import Purple.Purple.place.service.PlaceService;
import Purple.Purple.place.service.TagVectorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 데이터 초기화를 수행하는 Runner
 * 비동기로 실행되어 서버 시작을 차단하지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializationRunner implements ApplicationRunner {

    private final DataInitializationProperties properties;
    private final PlaceService placeService;
    private final TagVectorizationService tagVectorizationService;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isInitializeOnStartup()) {
            log.info("데이터 초기화가 비활성화되어 있습니다. (app.data.initialize-on-startup=false)");
            return;
        }

        log.info("=".repeat(80));
        log.info("데이터 초기화를 시작합니다...");
        log.info("설정: 장소 가져오기={}, 벡터화={}",
            properties.isFetchPlaces(),
            properties.isVectorizePlaces());
        log.info("=".repeat(80));

        // 비동기로 데이터 초기화 실행
        executeDataInitialization();
    }

    /**
     * 비동기로 데이터 초기화를 실행합니다.
     * 서버 시작을 차단하지 않기 위해 별도 스레드에서 실행됩니다.
     */
    @Async
    public void executeDataInitialization() {
        long startTime = System.currentTimeMillis();

        try {
            // 1단계: 장소 데이터 가져오기
            if (properties.isFetchPlaces()) {
                fetchPlacesData();
            } else {
                log.info("장소 데이터 가져오기가 비활성화되어 있습니다.");
            }

            // 2단계: 장소 벡터화
            if (properties.isVectorizePlaces()) {
                vectorizePlacesData();
            } else {
                log.info("장소 벡터화가 비활성화되어 있습니다.");
            }

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("=".repeat(80));
            log.info("데이터 초기화가 완료되었습니다! 소요 시간: {}초", duration);
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("데이터 초기화 중 오류가 발생했습니다: {}", e.getMessage(), e);
            log.warn("서버는 정상적으로 시작되었으나, 데이터 초기화에 실패했습니다.");
        }
    }

    /**
     * 장소 데이터를 Kakao API에서 가져옵니다.
     */
    private void fetchPlacesData() {
        try {
            log.info("-".repeat(80));
            log.info("[1/2] 장소 데이터 가져오기 시작...");
            log.info("천안시 전체 동네(27개)의 장소 데이터를 Kakao API에서 가져옵니다.");
            log.info("카테고리: 음식점, 카페, 문화시설, 관광명소, 공원, 쇼핑");
            log.info("주의: 이 작업은 시간이 오래 걸릴 수 있습니다 (약 10~30분).");
            log.info("-".repeat(80));

            long startTime = System.currentTimeMillis();
            placeService.fetchAllPlacesForCheonan();
            long duration = (System.currentTimeMillis() - startTime) / 1000;

            log.info("장소 데이터 가져오기 완료! 소요 시간: {}초", duration);

        } catch (Exception e) {
            log.error("장소 데이터 가져오기 실패: {}", e.getMessage(), e);
            throw new RuntimeException("장소 데이터 가져오기 실패", e);
        }
    }

    /**
     * 모든 장소에 대해 태그 벡터화를 수행합니다.
     */
    private void vectorizePlacesData() {
        try {
            log.info("-".repeat(80));
            log.info("[2/2] 장소 벡터화 시작...");
            log.info("모든 장소에 대해 태그 추출 및 벡터화를 수행합니다.");
            log.info("-".repeat(80));

            long startTime = System.currentTimeMillis();
            var result = tagVectorizationService.vectorizeAllPlaces();
            long duration = (System.currentTimeMillis() - startTime) / 1000;

            log.info("장소 벡터화 완료!");
            log.info("처리 결과 - 총: {}, 성공: {}, 실패: {}, 소요 시간: {}초",
                result.getTotalPlaces(),
                result.getSuccessCount(),
                result.getFailureCount(),
                duration);

        } catch (Exception e) {
            log.error("장소 벡터화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("장소 벡터화 실패", e);
        }
    }
}
