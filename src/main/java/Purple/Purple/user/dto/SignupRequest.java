package Purple.Purple.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "사용자 비밀번호", example = "password123", required = true)
    private String password;

    @Schema(description = "사용자 이름", example = "김퍼플", required = true)
    private String userName;

    @Schema(description = "닉네임", example = "보라돌이", required = false)
    private String nickname;
}
