package Purple.Purple.recommendations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@Schema(description = "Top 3 추천 동네 리스트 응답 DTO")
public class TopNeighborhoodsResponseDto {
    @Schema(example = "천안시")
    private String regionName;

    private List<RecommendedNeighborhoodDto> recommendedNeighborhoods;
}