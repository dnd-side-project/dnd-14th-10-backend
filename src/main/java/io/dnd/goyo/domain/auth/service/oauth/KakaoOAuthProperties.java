package io.dnd.goyo.domain.auth.service.oauth;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        List<String> allowedRedirectUris,
        String tokenUrl,
        String userInfoUrl
) {
}
