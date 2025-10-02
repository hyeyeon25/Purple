package Purple.Purple.user.dto;

import Purple.Purple.enums.Gender;
import Purple.Purple.user.entity.UserPersonalInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "사용자 정보 응답 DTO")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "보라돌이")
    private String nickname;

    @Schema(description = "사용자 성별", example = "MALE")
    private Gender gender;

    @Schema(description = "사용자 생일", example = "yyyy-MM-dd")
    private LocalDate birthDate;

    public UserResponse(Long userId, String email, String nickname, Gender gender, LocalDate birthDate) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
    }



}
