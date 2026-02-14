package io.dnd.goyo.domain.auth.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.enums.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthProvider implements OAuthProvider {

    private final RestClient restClient;
    private final KakaoOAuthProperties properties;

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code) {
        String accessToken = getAccessToken(code);
        KakaoUserInfoResponse userInfo = fetchUserInfo(accessToken);

        return new OAuthUserInfo(
                Provider.KAKAO,
                String.valueOf(userInfo.id()),
                userInfo.getNickname(),
                userInfo.getProfileImageUrl()
        );
    }

    private String getAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", properties.clientId());
        body.add("client_secret", properties.clientSecret());
        body.add("redirect_uri", properties.redirectUri());
        body.add("code", code);

        KakaoTokenResponse response = restClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    String errorBody = readErrorBody(res);
                    log.error("Kakao token error - status: {}, body: {}", res.getStatusCode(), errorBody);
                    throw new BusinessException(ErrorCode.OAUTH_AUTH_FAILED);
                })
                .body(KakaoTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new BusinessException(ErrorCode.OAUTH_AUTH_FAILED, "카카오 토큰 응답이 비어있습니다");
        }

        return response.accessToken();
    }

    private KakaoUserInfoResponse fetchUserInfo(String accessToken) {
        KakaoUserInfoResponse response = restClient.get()
                .uri(properties.userInfoUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, res) -> {
                    String errorBody = readErrorBody(res);
                    log.error("Kakao user info error - status: {}, body: {}", res.getStatusCode(), errorBody);
                    throw new BusinessException(ErrorCode.OAUTH_AUTH_FAILED);
                })
                .body(KakaoUserInfoResponse.class);

        if (response == null) {
            throw new BusinessException(ErrorCode.OAUTH_AUTH_FAILED, "카카오 사용자 정보가 비어있습니다");
        }

        return response;
    }

    private String readErrorBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read error response body", e);
            return "Unknown error";
        }
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") Integer expiresIn
    ) {
    }

    private record KakaoUserInfoResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
        String getNickname() {
            if (kakaoAccount != null && kakaoAccount.profile() != null) {
                return kakaoAccount.profile().nickname();
            }
            return null;
        }

        String getProfileImageUrl() {
            if (kakaoAccount != null && kakaoAccount.profile() != null) {
                return kakaoAccount.profile().profileImageUrl();
            }
            return null;
        }

        record KakaoAccount(Profile profile) {
            record Profile(
                    String nickname,
                    @JsonProperty("profile_image_url") String profileImageUrl
            ) {
            }
        }
    }
}
