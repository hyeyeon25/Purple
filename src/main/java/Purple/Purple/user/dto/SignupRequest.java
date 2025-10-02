package Purple.Purple.user.dto;

import Purple.Purple.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignupRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "사용자 비밀번호", example = "password123", required = true)
    private String password;

    @Schema(description = "닉네임", example = "보라돌이", required = false)
    private String nickname;

    @Schema(description = "사용자 성별", example = "MALE")
    private Gender gender;

    @Schema(description = "사용자 생일", example = "yyyy-MM-dd")
    private LocalDate birthDate;
}
