package Purple.Purple.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원정보 수정 요청 DTO")
public class UserUpdateRequest {

    @Schema(description = "수정할 닉네임", example = "새로운닉네임", required = false)
    private String nickname;

}
