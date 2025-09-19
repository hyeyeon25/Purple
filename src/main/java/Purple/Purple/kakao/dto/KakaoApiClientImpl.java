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

    // @Qualifier를 사용하여 어떤 WebClient Bean을 주입받을지 명확하게 지정합니다.
    public KakaoApiClientImpl(@Qualifier("kakaoWebClient") WebClient kakaoWebClient) {
        this.kakaoWebClient = kakaoWebClient;
    }

    @Override
    public KakaoPlaceSearchResponse searchPlaces(String keyword, Double longitude, Double latitude, Integer radius, Integer page) {
        log.info("카카오 장소 검색 API 호출: keyword={}, lon={}, lat={}, radius={}", keyword, longitude, latitude, radius);

        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json") // API 상세 경로
                        .queryParam("query", keyword)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", radius)
                        .queryParam("size", 15) // 한 번에 최대 15개까지 가져오도록 설정
                        .queryParam("page", page)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoApiKey) // 인증 헤더 추가
                .header("KA", "os/server-java-21 origin/purple-project-1.0")
                .retrieve() // HTTP 요청을 보내고 응답을 받습니다.

                // API 호출 실패 시 에러 처리
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            log.error("카카오 API 호출 실패: Status {}, Body {}", response.statusCode(), response.bodyToMono(String.class));
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException("카카오 API 호출에 실패했습니다. 응답: " + body)));
                        })

                // 응답받은 JSON 본문을 KakaoPlaceSearchResponse DTO로 변환합니다.
                .bodyToMono(KakaoPlaceSearchResponse.class)

                // Mono<T> 타입의 비동기 결과를 동기적으로 기다려서 T 타입의 객체를 얻습니다.
                .block();
    }
}
