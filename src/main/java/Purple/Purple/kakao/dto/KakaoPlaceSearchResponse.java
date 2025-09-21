package Purple.Purple.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;

/**
 *  Kakao Local Search API 전체 응답을 담는 DTO
 * - meta : 검색 결과에 대한 메타 정보(총 건수, 마지막 페이지 여부 등)
 * - documents : 실제 장소 목록(여러 개)
**/
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoPlaceSearchResponse {

    private KakaoMeta meta;
    private List<KakaoPlaceDocument> documents;
}
