package io.dnd.goyo.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dnd.goyo.domain.auth.service.oauth.OAuthUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 로그인 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuthLoginResponse(
        @Schema(description = "신규 사용자 여부")
        boolean isNewUser,

        @Schema(description = "액세스 토큰 (기존 사용자만)")
        String accessToken,

        @Schema(description = "리프레시 토큰 (기존 사용자만)")
        String refreshToken,

        @Schema(description = "액세스 토큰 만료 시간 (기존 사용자만, 밀리초)")
        Long expiresIn,

        @Schema(description = "사용자 정보 (기존 사용자만)")
        UserInfoResponse user,

        @Schema(description = "회원가입 토큰 (신규 사용자만)")
        String signupToken,

        @Schema(description = "OAuth 제공자로부터 받은 사용자 정보 (신규 사용자만)")
        OAuthInfo oauthInfo
) {
    @Schema(description = "OAuth 제공자 사용자 정보")
    public record OAuthInfo(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "프로필 이미지 URL")
            String profileImg
    ) {
    }

    public static OAuthLoginResponse forExistingUser(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserInfoResponse user
    ) {
        return new OAuthLoginResponse(
                false,
                accessToken,
                refreshToken,
                expiresIn,
                user,
                null,
                null
        );
    }

    public static OAuthLoginResponse forNewUser(
            String signupToken,
            OAuthUserInfo oauthUserInfo
    ) {
        return new OAuthLoginResponse(
                true,
                null,
                null,
                null,
                null,
                signupToken,
                new OAuthInfo(oauthUserInfo.name(), oauthUserInfo.profileImageUrl())
        );
    }
}
