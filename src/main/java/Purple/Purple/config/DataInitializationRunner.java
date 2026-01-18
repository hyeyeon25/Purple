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
 * 애플리케이션 시작 시 데이터 초기화 수행
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
            return;
        }

        log.info("[DataInit] 데이터 초기화 작업이 비동기로 시작됩니다. (Fetch={}, Vectorize={})",
                properties.isFetchPlaces(), properties.isVectorizePlaces());
        executeDataInitialization();
    }

    @Async
    public void executeDataInitialization() {
        long totalStartTime = System.currentTimeMillis();

        try {
            // 1. 장소 데이터 가져오기
            if (properties.isFetchPlaces()) {
                fetchPlacesData();
            }

            // 2. 장소 벡터화
            if (properties.isVectorizePlaces()) {
                vectorizePlacesData();
            }
            long duration = (System.currentTimeMillis() - totalStartTime) / 1000;
            log.info("[DataInit] 모든 데이터 초기화 완료 총 소요 시간: {}초", duration);

        } catch (Exception e) {
            log.error("[DataInit] 데이터 초기화 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }

    private void fetchPlacesData() {
        log.info("[DataInit] 1/2 장소 데이터 수집 시작 (Target: 천안시 전체 동네)");
        long startTime = System.currentTimeMillis();

        try {
            placeService.fetchAllPlacesForCheonan();
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            log.info("[DataInit] 1/2 장소 데이터 수집 완료 ({}초)", duration);
        } catch (Exception e) {
            log.error("[DataInit] 장소 수집 실패: {}", e.getMessage(), e);
            throw new RuntimeException("장소 데이터 가져오기 실패", e);
        }
    }

    private void vectorizePlacesData() {
        log.info("[DataInit] 2/2 장소 벡터화 작업 시작");
        long startTime = System.currentTimeMillis();

        try {
            var result = tagVectorizationService.vectorizeAllPlaces();
            long duration = (System.currentTimeMillis() - startTime) / 1000;

            log.info("[DataInit] 2/2 벡터화 완료 ({}초) - 총:{} / 성공:{} / 실패:{}",
                    duration, result.getTotalPlaces(), result.getSuccessCount(), result.getFailureCount());
        } catch (Exception e) {
            log.error("[DataInit] 벡터화 작업 실패: {}", e.getMessage(), e);
        }
    }
}