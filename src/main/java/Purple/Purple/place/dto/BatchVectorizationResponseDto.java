package Purple.Purple.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "배치 벡터화 응답 DTO")
public class BatchVectorizationResponseDto {

    @Schema(description = "전체 처리 대상 장소 수", example = "100")
    private Integer totalPlaces;

    @Schema(description = "성공한 장소 수", example = "95")
    private Integer successCount;

    @Schema(description = "실패한 장소 수", example = "5")
    private Integer failureCount;

    @Schema(description = "처리 시간 (초)", example = "12.5")
    private Double processingTimeSeconds;

    @Schema(description = "메시지", example = "배치 벡터화가 완료되었습니다.")
    private String message;
}