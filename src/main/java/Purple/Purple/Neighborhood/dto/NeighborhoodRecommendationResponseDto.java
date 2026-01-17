package Purple.Purple.Neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 동네 추천 응답 DTO (단일 동네)
 * 사용자 선호도와 가장 일치하는 동네의
 * 정보와 평균 유사도 점수를 반환
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "동네 추천 응답 DTO")
public class NeighborhoodRecommendationResponseDto {

    @Schema(description = "동네 ID (DB PK)", example = "5")
    private Integer neighborhoodId;

    @Schema(description = "동네 이름", example = "불당동")
    private String neighborhoodName;

    @Schema(description = "사용자 선호도와의 평균 유사도 점수 (0.0 ~ 1.0)", example = "0.753")
    private Double averageSimilarityScore;

    @Schema(description = "해당 동네에 속한 장소의 총 개수", example = "150")
    private Integer placeCount;

    @Schema(description = "추천 순위 (1, 2, 3...)", example = "1")
    private Integer rank;
}