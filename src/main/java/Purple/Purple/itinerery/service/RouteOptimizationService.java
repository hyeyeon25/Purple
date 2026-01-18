package Purple.Purple.itinerery.service;

import Purple.Purple.itinerery.exception.RouteOptimizationException;
import Purple.Purple.place.entity.PlaceEntity;
import Purple.Purple.tmap.dto.TmapFeature;
import Purple.Purple.tmap.dto.TmapPedestrianResponse;
import Purple.Purple.tmap.dto.TmapWalkingApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// TMAP API를 사용한 경로 최적화 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final TmapWalkingApiClient tmapWalkingApiClient;
    private static final double EARTH_RADIUS_KM = 6371.0;

    // 최적 경로 생성 (Nearest Neighbor 휴리스틱)
    public List<Integer> generateOptimalRoute(List<PlaceEntity> places) {
        if (places == null || places.isEmpty()) {
            return List.of();
        }
        if (places.size() == 1) {
            return List.of(places.get(0).getPlaceId());
        }

        log.info("=== 최적 경로 생성 시작 (총 {}개 장소) ===", places.size());

        try {
            List<PlaceEntity> unvisited = new ArrayList<>(places);
            List<Integer> route = new ArrayList<>();
            double totalDistance = 0.0;

            // 첫 번째 장소 선택 (좌표가 있는 첫 번째 장소)
            PlaceEntity current = selectStartPlace(unvisited);
            route.add(current.getPlaceId());
            unvisited.remove(current);
            log.info("1. 시작 장소: {} (위도: {}, 경도: {})",
                    current.getPlaceName(), current.getLatitude(), current.getLongitude());

            // Nearest Neighbor 알고리즘으로 순차적으로 가장 가까운 장소 선택
            int order = 2;
            while (!unvisited.isEmpty()) {
                PlaceEntity nearest = findNearestPlace(current, unvisited);
                double distance = calculateDistance(current, nearest);

                route.add(nearest.getPlaceId());
                totalDistance += distance;
                log.info("{}. 다음 장소: {} (거리: {:.2f}km, 누적: {:.2f}km)",
                        order++, nearest.getPlaceName(), distance, totalDistance);

                unvisited.remove(nearest);
                current = nearest;
            }

            log.info("=== 최적 경로 생성 완료 (총 거리: {:.2f}km) ===", totalDistance);
            log.info("경로 순서: {}", route);

            return route;

        } catch (Exception e) {
            log.error("최적 경로 생성 중 오류 발생", e);
            throw new RouteOptimizationException("최적 경로 생성에 실패했습니다.", e);
        }
    }


    // 두 장소 간의 도보 거리 계산
    // TMAP API를 사용, 실패 시 Haversine 공식으로 폴백
    // @return 도보 거리 (km)
    public double calculateDistance(PlaceEntity from, PlaceEntity to) {
        if (!hasValidCoordinates(from) || !hasValidCoordinates(to)) {
            return Double.MAX_VALUE;
        }

        try {
            // TMAP API로 실제 도보 거리 조회
            TmapPedestrianResponse response = tmapWalkingApiClient.getPedestrianRoute(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude()
            );

            if (isValidResponse(response)) {
                TmapFeature firstFeature = response.getFeatures().get(0);
                if (firstFeature.getProperties() != null &&
                        firstFeature.getProperties().getTotalDistance() != null) {

                    double distanceKm = firstFeature.getProperties().getTotalDistance() / 1000.0;
                    log.debug("TMAP 도보 거리: {} -> {} = {:.2f}km (소요시간: {}초)",
                            from.getPlaceName(), to.getPlaceName(), distanceKm,
                            firstFeature.getProperties().getTotalTime());
                    return distanceKm;
                }
            }

            // API 응답이 유효하지 않을 시 직선 거리로 폴백
            log.warn("TMAP API 응답이 유효하지 않아 직선 거리로 계산합니다: {} -> {}",
                    from.getPlaceName(), to.getPlaceName());
            return calculateHaversineDistance(from, to);

        } catch (Exception e) {
            // API 호출 실패 시 직선 거리로 폴백
            log.warn("TMAP API 호출 실패, 직선 거리로 계산합니다: {} -> {} ({})",
                    from.getPlaceName(), to.getPlaceName(), e.getMessage());
            return calculateHaversineDistance(from, to);
        }
    }

    // 시작 장소 선택
    private PlaceEntity selectStartPlace(List<PlaceEntity> places) {
        return places.stream()
                .filter(this::hasValidCoordinates)
                .findFirst()
                .orElse(places.get(0));
    }

    // 현재 위치에서 가장 가까운 장소 탐색
    private PlaceEntity findNearestPlace(PlaceEntity current, List<PlaceEntity> candidates) {
        PlaceEntity nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (PlaceEntity candidate : candidates) {
            if (!hasValidCoordinates(candidate)) {
                continue;
            }

            double distance = calculateDistance(current, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }

        // 좌표 없는 장소는 마지막에 추가
        return nearest != null ? nearest : candidates.get(0);
    }

    // Haversine 공식으로 직선 거리 계산
    private double calculateHaversineDistance(PlaceEntity from, PlaceEntity to) {
        if (!hasValidCoordinates(from) || !hasValidCoordinates(to)) {
            return Double.MAX_VALUE;
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // 장소에 유효한 좌표가 있는지 확인
    private boolean hasValidCoordinates(PlaceEntity place) {
        return place != null &&
                place.getLatitude() != null &&
                place.getLongitude() != null;
    }

    // TMAP API 응답 유효한지 확인
    private boolean isValidResponse(TmapPedestrianResponse response) {
        return response != null &&
                response.getFeatures() != null &&
                !response.getFeatures().isEmpty();
    }
}

