package Purple.Purple.Neighborhood.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * 사용자 선호도 요청 DTO
 *
 * 사용자가 선호하는 태그 리스트를 입력받아
 * 동네 추천에 활용합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 선호도 태그 요청 DTO")
public class UserPreferenceRequestDto {

    /**
     * 사용자가 선호하는 태그 리스트
     * 예: ["카페", "데이트", "실내"]
     *
     * 태그 사전:
     * - 장소 유형: "음식점", "카페", "액티비티", "문화"
     * - 실내/실외: "실내", "실외"
     * - 시간대: "아침추천", "점심추천", "오후추천", "저녁추천", "밤추천"
     * - 동행: "데이트", "가족", "친구", "혼자"
     */
    @Schema(description = "사용자가 선호하는 태그 리스트", example = "[\"카페\", \"데이트\", \"실내\"]")
    @NotEmpty(message = "선호 태그는 최소 1개 이상 입력해야 합니다.")
    private List<String> tags;
}