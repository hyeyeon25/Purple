package Purple.Purple.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
/**
 * Kakao Local Search API의 meta 부분
 * - 검색 결과의 전체 개수(totalCount), 페이지 가능 개수(pageableCount),
 *   마지막 페이지 여부(isEnd)를 알려줌
 *
 * 페이지네이션 처리할 때 꼭 필요한 정보
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoMeta {
    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("pageable_count")
    private Integer pageableCount;

    @JsonProperty("is_end")
    private Boolean isEnd;
}
