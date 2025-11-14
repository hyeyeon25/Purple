package Purple.Purple.preferences.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "성향 조회/응답 DTO")
public class UserPreferenceResponse {
    private Long userid;
    private int foodPreference;
    private int desertPreference;
    private int culturePreference;
    private List<Integer> timePreference;
    private boolean indoorPreference;
    private boolean extrovertPreference;
    private int activityPreference;
}