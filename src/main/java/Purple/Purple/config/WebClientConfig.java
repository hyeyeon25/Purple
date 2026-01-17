package Purple.Purple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${kakao.api.base-url}")
    private String kakaoApiBaseUrl;

    @Value("${tmap.api.base-url}")
    private String tmapApiBaseUrl;

    @Bean
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoApiBaseUrl)
                .build();
    }

    @Bean
    public WebClient tmapWebClient() {
        // Tmap API용 WebClient 인스턴스를 생성합니다.
        return WebClient.builder()
                .baseUrl(tmapApiBaseUrl)
                .build();
    }
}