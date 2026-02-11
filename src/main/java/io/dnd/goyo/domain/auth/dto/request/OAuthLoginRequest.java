package io.dnd.goyo.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth 로그인 요청")
public record OAuthLoginRequest(
        @Schema(description = "OAuth 인가 코드", example = "authorization_code_here")
        @NotBlank(message = "인가 코드는 필수입니다")
        String code
) {
}
