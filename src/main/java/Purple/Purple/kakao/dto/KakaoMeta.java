package Purple.Purple.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
/**
 * Kakao Local Search API에서
 * - 검색 결과의 전체 개수(total_count), 페이지 가능 개수(pageable_count),
 *   마지막 페이지 여부(is_end)를 알려줌
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
