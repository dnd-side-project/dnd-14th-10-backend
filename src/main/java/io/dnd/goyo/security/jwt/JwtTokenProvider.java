package io.dnd.goyo.security.jwt;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        validateSecretKey(jwtProperties.secret());
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    private void validateSecretKey(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HMAC-SHA256");
        }
    }

    public String createAccessToken(Long userId, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "access")
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Long parseRefreshToken(String token) {
        Claims claims = getClaims(token);
        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return Long.parseLong(claims.getSubject());
    }

    public String createSignupToken(Provider provider, String providerId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.signupTokenExpiration());

        return Jwts.builder()
                .subject(providerId)
                .claim("type", "signup")
                .claim("provider", provider.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public SignupTokenInfo parseSignupToken(String token) {
        Claims claims = getClaims(token);
        String type = claims.get("type", String.class);
        if (!"signup".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN);
        }
        String providerId = claims.getSubject();
        Provider provider = Provider.from(claims.get("provider", String.class));
        return new SignupTokenInfo(provider, providerId);
    }

    public record AccessTokenInfo(Long userId, String role) {
    }

    public AccessTokenInfo parseAccessToken(String token) {
        Claims claims = getClaims(token);
        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        return new AccessTokenInfo(userId, role);
    }

    public record SignupTokenInfo(Provider provider, String providerId) {
    }

    public long getAccessTokenExpiration() {
        return jwtProperties.accessTokenExpiration();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
