package Purple.Purple.Neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * 사용자 선호도 요청 DTO
 *
 * 사용자가 선호하는 태그 리스트를 입력받아
 * 동네 추천에 활용.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 선호도 태그 요청 DTO")
public class UserPreferenceRequestDto {


    @Schema(description = "사용자가 선호하는 태그 리스트", example = "[\"카페\", \"데이트\", \"실내\"]")
    @NotEmpty(message = "선호 태그는 최소 1개 이상 입력해야 합니다.")
    private List<String> tags;
}