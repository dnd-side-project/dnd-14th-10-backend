package io.dnd.goyo.security.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public record CookieProperties(
        String domain,
        boolean secure,
        String sameSite,
        long maxAge
) {
}
