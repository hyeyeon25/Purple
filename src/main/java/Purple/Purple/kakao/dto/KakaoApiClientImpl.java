package Purple.Purple.kakao.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Primary
@Slf4j
public class KakaoApiClientImpl implements KakaoApiClient {

    private final WebClient kakaoWebClient;

    @Value("${kakaomap.api.key}")
    private String kakaoApiKey;

    public KakaoApiClientImpl(@Qualifier("kakaoWebClient") WebClient kakaoWebClient) {
        this.kakaoWebClient = kakaoWebClient;
    }

    @Override
    public KakaoPlaceSearchResponse searchPlaces(String keyword, Double longitude, Double latitude, Integer radius, Integer page) {

        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", radius)
                        .queryParam("size", 15)
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .header("KA", "os/server-java-21 origin/purple-project-1.0")
                .retrieve()

                // API 호출 실패 시 에러 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("카카오 API 호출 실패: Status {}, Body {}", response.statusCode(), response.bodyToMono(String.class));
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("카카오 API 호출에 실패했습니다. 응답: " + body)));
                        })
                .bodyToMono(KakaoPlaceSearchResponse.class)
                .block();
    }
}
