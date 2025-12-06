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
@Schema(description = "태그 벡터화 요청 DTO")
public class TagVectorizationRequestDto {

    @Schema(description = "벡터화할 태그 리스트 (선택적, 미입력 시 장소 특성에서 자동 추출)", example = "[\"맛집\", \"데이트\", \"실내\"]")
    private List<String> tags;

    @Schema(description = "기존 태그 덮어쓰기 여부", example = "true", defaultValue = "false")
    @Builder.Default
    private Boolean overwrite = false;
}