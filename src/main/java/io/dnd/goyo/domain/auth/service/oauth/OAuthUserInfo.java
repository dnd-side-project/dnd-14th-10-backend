package io.dnd.goyo.domain.auth.service.oauth;

import io.dnd.goyo.domain.user.enums.Provider;

public record OAuthUserInfo(
        Provider provider,
        String providerId,
        String name,
        String profileImageUrl
) {
}
