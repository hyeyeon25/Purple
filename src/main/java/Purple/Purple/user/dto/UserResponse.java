package Purple.Purple.user.dto;

import Purple.Purple.user.entity.UserPersonalInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 이름", example = "김퍼플")
    private String userName;

    @Schema(description = "닉네임", example = "보라돌이")
    private String nickname;

    public UserResponse(Long userId, String email, String userName, String nickname) {
        this.userId = userId;
        this.email = email;
        this.userName = userName;
        this.nickname = nickname;
    }

}
