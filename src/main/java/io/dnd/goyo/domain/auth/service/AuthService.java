package io.dnd.goyo.domain.auth.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.LoginResponse;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.TokenResponse;
import io.dnd.goyo.domain.auth.dto.response.UserInfoResponse;
import io.dnd.goyo.domain.auth.service.oauth.OAuthProvider;
import io.dnd.goyo.domain.auth.service.oauth.OAuthUserInfo;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import io.dnd.goyo.security.jwt.JwtTokenProvider.SignupTokenInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final Map<Provider, OAuthProvider> oauthProviders;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            List<OAuthProvider> oauthProviderList,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.oauthProviders = oauthProviderList.stream()
                .collect(Collectors.toMap(OAuthProvider::getProvider, Function.identity()));
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public OAuthLoginResponse oauthLogin(Provider provider, String code) {
        OAuthProvider oauthProvider = getOAuthProvider(provider);
        OAuthUserInfo userInfo = oauthProvider.getUserInfo(code);

        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                userInfo.provider(), userInfo.providerId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            validateUserStatus(user);
            return createLoginResponseForExistingUser(user);
        }

        return createResponseForNewUser(userInfo);
    }

    @Transactional
    public LoginResponse signup(SignupRequest request) {
        SignupTokenInfo tokenInfo = jwtTokenProvider.parseSignupToken(request.signupToken());
        Provider provider = tokenInfo.provider();
        String providerId = tokenInfo.providerId();

        if (userRepository.existsByNicknameAndStatusNot(request.nickname(), UserStatus.DELETED)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .name(request.name())
                .nickname(request.nickname())
                .birth(request.birth())
                .gender(request.gender())
                .profileImg(request.profileImg())
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .locationConsent(request.locationConsent())
                .regionCode(request.regionCode())
                .build();

        User savedUser = userRepository.save(user);

        return createLoginResponse(savedUser);
    }

    public TokenResponse refresh(String refreshToken) {
        Long userId = jwtTokenProvider.parseRefreshToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateUserStatus(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiration()
        );
    }

    private OAuthProvider getOAuthProvider(Provider provider) {
        OAuthProvider oauthProvider = oauthProviders.get(provider);
        if (oauthProvider == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "지원하지 않는 OAuth 제공자입니다: " + provider);
        }
        return oauthProvider;
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
    }

    private OAuthLoginResponse createLoginResponseForExistingUser(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return OAuthLoginResponse.forExistingUser(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                UserInfoResponse.from(user)
        );
    }

    private OAuthLoginResponse createResponseForNewUser(OAuthUserInfo userInfo) {
        String signupToken = jwtTokenProvider.createSignupToken(
                userInfo.provider(), userInfo.providerId());

        return OAuthLoginResponse.forNewUser(signupToken, userInfo);
    }

    private LoginResponse createLoginResponse(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpiration(),
                UserInfoResponse.from(user)
        );
    }
}
