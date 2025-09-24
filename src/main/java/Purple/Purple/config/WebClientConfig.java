package Purple.Purple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // application.yml에 설정한 카카오 API 기본 URL을 주입받습니다.
    @Value("${kakao.api.base-url}")
    private String kakaoApiBaseUrl;

    @Bean
    public WebClient kakaoWebClient() {
        // 기본 URL을 설정하여 WebClient 인스턴스를 생성합니다.
        // 이 Bean을 주입받아 사용하는 모든 곳에서 이 기본 URL을 사용하게 됩니다.
        return WebClient.builder()
                .baseUrl(kakaoApiBaseUrl)
                .build();
    }
}