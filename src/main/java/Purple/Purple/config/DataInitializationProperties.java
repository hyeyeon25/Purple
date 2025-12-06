package Purple.Purple.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 데이터 초기화 설정을 관리하는 클래스
 * application.properties에서 app.data.* 프로퍼티를 바인딩합니다.
 */
@Configuration
@ConfigurationProperties(prefix = "app.data")
@Getter
@Setter
public class DataInitializationProperties {

    /**
     * 서버 시작 시 데이터 초기화 실행 여부
     * 기본값: false
     */
    private boolean initializeOnStartup = false;

    /**
     * 장소 데이터 가져오기 실행 여부
     * 기본값: true
     */
    private boolean fetchPlaces = true;

    /**
     * 장소 벡터화 실행 여부
     * 기본값: true
     */
    private boolean vectorizePlaces = true;
}
