package Purple.Purple.recommendations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "추천 동네 정보 DTO")
public class RecommendedNeighborhoodDto {
    @Schema(example = "11")
    private Integer neighborhoodId;

    @Schema(example = "신부동")
    private String neighborhoodName;

    @Schema(example = "36.8194")
    private Float neighborhoodLatitude;

    @Schema(example = "127.1543")
    private Float neighborhoodLongitude;

    @Schema(example = "92.5")
    private Float matchScore;
}