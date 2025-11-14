package Purple.Purple.preferences.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "성향 등록 요청 DTO")
public class UserPreferenceRequest {
    @Schema(description = "외향 성향 코드 (0: 매우 외향적, 1: 보통 외향적, 2: 보통 내향적, 3: 매우 내향적)", example = "2")
    private int extrovertPreference;

    @Schema(description = "실내 선호도 코드 (0~100, 높을수록 실내 선호)", example = "50")
    private int indoorPreference;

    @Schema(description = "시간대 성향 코드 (예: 0: 오전, 1: 오후, 2: 밤, 3: 새벽)", example = "[2,3]")
    private List<Integer> timePreference;

    @Schema(description = "음식 성향 코드 (예: 0 : 한식, 1 : 양식, 2 : 아시안, 3:이색/퓨전, 4:분식, 5:건강식, 6:주류)", example = "1")
    private int foodPreference;

    @Schema(description = "디저트 성향 코드 (예 0~3)", example = "2")
    private int desertPreference;

    @Schema(description = "활동 성향 코드 (예 0~3)", example = "3")
    private int activityPreference;

    @Schema(description = "문화 활동 성향 코드 (예 0~3)", example = "0")
    private int culturePreference;
}
