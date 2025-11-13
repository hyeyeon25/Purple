package Purple.Purple.rout.service;

import Purple.Purple.rout.dto.PlaceResponseDto;
import Purple.Purple.rout.entity.Location;
import Purple.Purple.rout.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class RouteService {

    private final LocationRepository locationRepository;

    private double[][] distance;
    private boolean[] visited;
    private double minDistance;
    private List<String> bestRoute;

    public List<PlaceResponseDto> findOptimalRoute() {
        List<Location> places = locationRepository.findAll();

        int n = places.size();
        distance = new double[n][n];
        visited = new boolean[n];
        minDistance = Double.MAX_VALUE;
        bestRoute = new ArrayList<>();

        // 거리 행렬 계산
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distance[i][j] = getDistance(places.get(i), places.get(j));
                }
            }
        }

        // DFS 시작
        dfs(places, new ArrayList<>(), 0, 0, 0);

        List<PlaceResponseDto> result = new ArrayList<>();
        for(String name : bestRoute) {
            Location loc = locationRepository.findByName(name).
                    orElseThrow(() -> new RuntimeException("장소 조회 실패: "+name));

           result.add(
                    PlaceResponseDto.builder()
                            .id(String.valueOf(loc.getId()))
                            .name(loc.getName())
                            .address(loc.getAddress())
                            .phone(loc.getPhone())
                            .mapUrl("https://map.kakao.com/link/map/" + loc.getName() + "," + loc.getLatitude() + "," + loc.getLongitude())
                            .aiSummary(loc.getAiSummary())
                            .build()
            );
        }
        return result;
    }

    private void dfs(List<Location> places, List<String> path, int depth, double dist, int current) {
        if (depth == places.size()) {
            if (dist < minDistance) {
                minDistance = dist;
                bestRoute = new ArrayList<>(path);
            }
            return;
        }

        for (int i = 0; i < places.size(); i++) {
            if (!visited[i]) {
                visited[i] = true;
                path.add(places.get(i).getName());

                double addedDist = (depth > 0) ? distance[current][i] : 0;
                dfs(places, path, depth + 1, dist + addedDist, i);

                visited[i] = false;
                path.remove(path.size() - 1);
            }
        }
    }

    private double getDistance(Location a, Location b) {
        final int R = 6371; // 지구 반경 (km)
        double latDist = Math.toRadians(b.getLatitude() - a.getLatitude());
        double lonDist = Math.toRadians(b.getLongitude() - a.getLongitude());
        double hav = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(a.getLatitude())) * Math.cos(Math.toRadians(b.getLatitude()))
                * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        return R * 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));
    }
}
