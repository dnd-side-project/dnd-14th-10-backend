package io.dnd.goyo.domain.auth.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.auth.dto.AuthTokens;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.UserInfoResponse;
import io.dnd.goyo.domain.auth.service.oauth.OAuthProvider;
import io.dnd.goyo.domain.auth.service.oauth.OAuthUserInfo;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import io.dnd.goyo.security.jwt.JwtTokenProvider.RefreshTokenInfo;
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
    private final UserStatsRepository userStatsRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenVersionService tokenVersionService;

    public AuthService(
            List<OAuthProvider> oauthProviderList,
            UserRepository userRepository,
            UserStatsRepository userStatsRepository,
            JwtTokenProvider jwtTokenProvider,
            TokenVersionService tokenVersionService
    ) {
        this.oauthProviders = oauthProviderList.stream()
                .collect(Collectors.toMap(OAuthProvider::getProvider, Function.identity()));
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenVersionService = tokenVersionService;
    }

    public OAuthLoginResponse oauthLogin(Provider provider, String code, String redirectUri) {
        OAuthProvider oauthProvider = getOAuthProvider(provider);
        OAuthUserInfo userInfo = oauthProvider.getUserInfo(code, redirectUri);

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
    public AuthTokens signup(SignupRequest request) {
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
        userStatsRepository.save(UserStats.of(savedUser));

        return createAuthTokens(savedUser);
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        RefreshTokenInfo tokenInfo = jwtTokenProvider.parseRefreshToken(refreshToken);

        User user = userRepository.findById(tokenInfo.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateUserStatus(user);

        if (tokenInfo.tokenVersion() != user.getTokenVersion()) {
            tokenVersionService.incrementTokenVersionInNewTransaction(tokenInfo.userId());
            throw new BusinessException(ErrorCode.TOKEN_REUSED);
        }

        user.incrementTokenVersion();

        return createTokenPair(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        RefreshTokenInfo tokenInfo = jwtTokenProvider.parseRefreshToken(refreshToken);
        User user = userRepository.findById(tokenInfo.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.incrementTokenVersion();
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

    private AuthTokens createTokenPair(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getTokenVersion());
        return new AuthTokens(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpiration());
    }

    private OAuthLoginResponse createLoginResponseForExistingUser(User user) {
        AuthTokens tokens = createTokenPair(user);

        return OAuthLoginResponse.forExistingUser(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                UserInfoResponse.from(user)
        );
    }

    private OAuthLoginResponse createResponseForNewUser(OAuthUserInfo userInfo) {
        String signupToken = jwtTokenProvider.createSignupToken(
                userInfo.provider(), userInfo.providerId());

        return OAuthLoginResponse.forNewUser(signupToken, userInfo);
    }

    private AuthTokens createAuthTokens(User user) {
        AuthTokens tokens = createTokenPair(user);

        return new AuthTokens(
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.expiresIn(),
                UserInfoResponse.from(user)
        );
    }
}
