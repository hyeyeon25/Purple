package Purple.Purple.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "태그 벡터화 응답 DTO")
public class TagVectorizationResponseDto {

    @Schema(description = "장소 ID", example = "1")
    private Integer placeId;

    @Schema(description = "장소명", example = "스타벅스 천안점")
    private String placeName;

    @Schema(description = "추출된 태그 리스트", example = "[\"카페\", \"실내\", \"오후추천\"]")
    private List<String> tags;

    @Schema(description = "정규화된 벡터", example = "[0.5, 0.3, 0.2, ...]")
    private List<Double> vector;

    @Schema(description = "벡터 차원", example = "10")
    private Integer vectorDimension;

    @Schema(description = "벡터화 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "메시지", example = "벡터화가 완료되었습니다.")
    private String message;
}