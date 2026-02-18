package io.dnd.goyo.domain.auth.dto;

import io.dnd.goyo.domain.auth.dto.response.UserInfoResponse;

public record AuthTokens(String accessToken, String refreshToken, long expiresIn, UserInfoResponse user) {

    public AuthTokens(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, expiresIn, null);
    }
}
