package io.dnd.goyo.domain.auth.service.oauth;

import io.dnd.goyo.domain.user.enums.Provider;

public interface OAuthProvider {

    Provider getProvider();

    OAuthUserInfo getUserInfo(String code, String redirectUri);
}
