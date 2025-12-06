package Purple.Purple.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapProperties {

    private Integer totalDistance;  // 총 거리 (미터) - Point 타입의 첫 번째 Feature에만 존재

    private Integer totalTime;  // 총 시간 (초) - Point 타입의 첫 번째 Feature에만 존재

    private Integer distance;  // 구간 거리 (미터) - LineString 타입

    private Integer time;  // 구간 시간 (초) - LineString 타입

    private String description;  // 경로 설명

    private Integer turnType;  // 회전 타입

    private String pointType;  // 지점 타입 ("SP": 출발, "EP": 도착, "GP": 경유)
}
