package Purple.Purple.kakao.dto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// 실제 Kakao API를 호출하는 클라이언트 구현체

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class KakaoApiClientImpl implements KakaoApiClient {

    private final WebClient kakaoWebClient; // WebClientConfig에서 등록한 Bean을 주입받습니다.

    // application.yml에 설정한 카카오 REST API 키를 주입받습니다.
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

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
                // 실제 대용량 트래픽 처리 시에는 block() 대신 비동기 파이프라인을 그대로 사용하는 것이 좋습니다.
                .block();
    }
}