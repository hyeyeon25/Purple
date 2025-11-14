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

    @Schema(description = "실내 선호 여부 (true=실내, false=실외)", example = "true")
    private boolean indoorPreference;

    @Schema(description = "시간대 성향 코드 (예: 0: 오전, 1: 오후, 2: 밤, 3: 새벽)", example = "[2,3]")
    private List<Integer> timePreference;


    @Schema(description = "음식 성향 코드 (예: 0 : 한식, 1 : 양식, 2 : 일식, 3 : 중식, 4 : 아시안, 5 : 이색/퓨전, 6 : 분식)", example = "[0,1,2]")
    private List<Integer> foodPreference;

    @Schema(description = "디저트 성향 코드 (예 0~3)", example = "2")
    private int desertPreference;

    @Schema(description = "활동 성향 코드 (예 0~3)", example = "3")
    private int activityPreference;

    @Schema(description = "문화 활동 성향 코드 (예 0~3)", example = "0")
    private int culturePreference;







}
