package io.dnd.goyo.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인/회원가입 완료 응답")
public record LoginResponse(
        @Schema(description = "액세스 토큰")
        String accessToken,

        @Schema(description = "액세스 토큰 만료 시간 (밀리초)", example = "1800000")
        long expiresIn,

        @Schema(description = "사용자 정보")
        UserInfoResponse user
) {
}
