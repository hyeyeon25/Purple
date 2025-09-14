package Purple.Purple.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class GoogleOauthRequest {

    @Schema(
            description = "구글 OAuth 인증 후 발급받은 id_token 값",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij..."
    )
    private String idToken;
}
