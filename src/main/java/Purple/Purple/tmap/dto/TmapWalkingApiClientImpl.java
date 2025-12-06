package Purple.Purple.tmap.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TmapWalkingApiClientImpl implements TmapWalkingApiClient {

    private final WebClient tmapWebClient;

    @Value("${tmap.api.key}")
    private String tmapApiKey;

    public TmapWalkingApiClientImpl(@Qualifier("tmapWebClient") WebClient tmapWebClient) {
        this.tmapWebClient = tmapWebClient;
    }

    @Override
    public TmapPedestrianResponse getPedestrianRoute(
            Double startX,
            Double startY,
            Double endX,
            Double endY
    ) {
        return getPedestrianRouteWithWaypoints(startX, startY, endX, endY, null);
    }

    @Override
    public TmapPedestrianResponse getPedestrianRouteWithWaypoints(
            Double startX,
            Double startY,
            Double endX,
            Double endY,
            String passList
    ) {
        log.info("Tmap 도보 경로 API 호출: start=({}, {}), end=({}, {})",
                startX, startY, endX, endY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("startX", String.valueOf(startX));
        requestBody.put("startY", String.valueOf(startY));
        requestBody.put("endX", String.valueOf(endX));
        requestBody.put("endY", String.valueOf(endY));
        requestBody.put("reqCoordType", "WGS84GEO");
        requestBody.put("resCoordType", "EPSG3857");
        requestBody.put("startName", "출발");
        requestBody.put("endName", "도착");

        if (passList != null && !passList.isEmpty()) {
            requestBody.put("passList", passList);
        }

        return tmapWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/tmap/routes/pedestrian")
                        .queryParam("version", "1")
                        .queryParam("format", "json")
                        .build())
                .header("appKey", tmapApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("Tmap API 호출 실패: Status {}", response.statusCode());
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("응답 본문: {}", body);
                                        return Mono.error(new RuntimeException("Tmap API 호출에 실패했습니다. 응답: " + body));
                                    });
                        })
                .bodyToMono(TmapPedestrianResponse.class)
                .block();
    }
}
