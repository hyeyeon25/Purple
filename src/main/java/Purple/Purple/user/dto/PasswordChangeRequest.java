package Purple.Purple.user.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 변경 요청 DTO")
public class PasswordChangeRequest {

    @Schema(description = "현재 비밀번호", example = "oldPassword123", required = true)
    private String currentPassword;

    @Schema(description = "새로운 비밀번호", example = "newPassword456", required = true)
    private String newPassword;
}
