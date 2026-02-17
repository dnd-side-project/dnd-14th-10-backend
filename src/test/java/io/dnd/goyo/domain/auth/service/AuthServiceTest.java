package io.dnd.goyo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.LoginResponse;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.TokenResponse;
import io.dnd.goyo.domain.auth.service.oauth.OAuthProvider;
import io.dnd.goyo.domain.auth.service.oauth.OAuthUserInfo;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import io.dnd.goyo.security.jwt.JwtTokenProvider.SignupTokenInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OAuthProvider kakaoOAuthProvider;

    private AuthService createAuthService() {
        given(kakaoOAuthProvider.getProvider()).willReturn(Provider.KAKAO);
        return new AuthService(List.of(kakaoOAuthProvider), userRepository, jwtTokenProvider);
    }

    private User createActiveUser() {
        return User.builder()
                .name("김고요")
                .nickname("고요한여행자")
                .gender(Gender.MALE)
                .provider(Provider.KAKAO)
                .providerId("kakao_123")
                .role(UserRole.USER)
                .build();
    }

    @Nested
    @DisplayName("OAuth 로그인 시")
    class OAuthLogin {

        @Test
        void 기존_사용자면_토큰과_사용자정보_반환() {
            // given
            AuthService authService = createAuthService();
            User user = createActiveUser();
            OAuthUserInfo userInfo = new OAuthUserInfo(Provider.KAKAO, "kakao_123", "김고요", null);

            given(kakaoOAuthProvider.getUserInfo("auth_code", "http://localhost:8080/test.html")).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao_123"))
                    .willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn("access_token");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh_token");
            given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1800000L);

            // when
            OAuthLoginResponse response = authService.oauthLogin(Provider.KAKAO, "auth_code", "http://localhost:8080/test.html");

            // then
            assertThat(response.isNewUser()).isFalse();
            assertThat(response.accessToken()).isEqualTo("access_token");
            assertThat(response.refreshToken()).isEqualTo("refresh_token");
            assertThat(response.user()).isNotNull();
        }

        @Test
        void 신규_사용자면_회원가입토큰_반환() {
            // given
            AuthService authService = createAuthService();
            OAuthUserInfo userInfo = new OAuthUserInfo(Provider.KAKAO, "kakao_new", "새유저", "profile.jpg");

            given(kakaoOAuthProvider.getUserInfo("auth_code", "http://localhost:8080/test.html")).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao_new"))
                    .willReturn(Optional.empty());
            given(jwtTokenProvider.createSignupToken(Provider.KAKAO, "kakao_new"))
                    .willReturn("signup_token");

            // when
            OAuthLoginResponse response = authService.oauthLogin(Provider.KAKAO, "auth_code", "http://localhost:8080/test.html");

            // then
            assertThat(response.isNewUser()).isTrue();
            assertThat(response.signupToken()).isEqualTo("signup_token");
            assertThat(response.oauthInfo().name()).isEqualTo("새유저");
        }

        @Test
        void 차단된_사용자면_예외_발생() {
            // given
            AuthService authService = createAuthService();
            User blockedUser = org.mockito.Mockito.mock(User.class);
            given(blockedUser.getStatus()).willReturn(UserStatus.BLOCKED);
            OAuthUserInfo userInfo = new OAuthUserInfo(Provider.KAKAO, "kakao_blocked", "차단유저", null);

            given(kakaoOAuthProvider.getUserInfo("auth_code", "http://localhost:8080/test.html")).willReturn(userInfo);
            given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao_blocked"))
                    .willReturn(Optional.of(blockedUser));

            // when & then
            assertThatThrownBy(() -> authService.oauthLogin(Provider.KAKAO, "auth_code", "http://localhost:8080/test.html"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_BLOCKED);
        }

        @Test
        void 지원하지_않는_provider면_예외_발생() {
            // given
            AuthService authService = createAuthService();

            // when & then
            assertThatThrownBy(() -> authService.oauthLogin(Provider.NAVER, "auth_code", "http://localhost:8080/test.html"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("지원하지 않는 OAuth 제공자입니다");
        }
    }

    @Nested
    @DisplayName("회원가입 시")
    class Signup {

        @Test
        void 정상_요청이면_사용자_생성_후_토큰_반환() {
            // given
            AuthService authService = createAuthService();
            SignupRequest request = new SignupRequest(
                    "signup_token", "김고요", "고요한여행자", Gender.MALE,
                    LocalDate.of(1995, 3, 15), null, null, null
            );
            SignupTokenInfo tokenInfo = new SignupTokenInfo(Provider.KAKAO, "kakao_123");

            given(jwtTokenProvider.parseSignupToken("signup_token")).willReturn(tokenInfo);
            given(userRepository.existsByNicknameAndStatusNot("고요한여행자", UserStatus.DELETED)).willReturn(false);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn("access_token");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh_token");
            given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1800000L);

            // when
            LoginResponse response = authService.signup(request);

            // then
            assertThat(response.accessToken()).isEqualTo("access_token");
            assertThat(response.refreshToken()).isEqualTo("refresh_token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        void locationConsent와_regionCode_포함_시_정상_생성() {
            // given
            AuthService authService = createAuthService();
            SignupRequest request = new SignupRequest(
                    "signup_token", "김고요", "고요한여행자", Gender.FEMALE,
                    LocalDate.of(1995, 3, 15), null, true, 1168010100L
            );
            SignupTokenInfo tokenInfo = new SignupTokenInfo(Provider.KAKAO, "kakao_123");

            given(jwtTokenProvider.parseSignupToken("signup_token")).willReturn(tokenInfo);
            given(userRepository.existsByNicknameAndStatusNot("고요한여행자", UserStatus.DELETED)).willReturn(false);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn("access_token");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh_token");
            given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1800000L);

            // when
            LoginResponse response = authService.signup(request);

            // then
            assertThat(response).isNotNull();
            verify(userRepository).save(any(User.class));
        }

        @Test
        void 중복_닉네임이면_예외_발생() {
            // given
            AuthService authService = createAuthService();
            SignupRequest request = new SignupRequest(
                    "signup_token", "김고요", "중복닉네임", Gender.MALE,
                    null, null, null, null
            );
            SignupTokenInfo tokenInfo = new SignupTokenInfo(Provider.KAKAO, "kakao_123");

            given(jwtTokenProvider.parseSignupToken("signup_token")).willReturn(tokenInfo);
            given(userRepository.existsByNicknameAndStatusNot("중복닉네임", UserStatus.DELETED)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
        }

        @Test
        void 유효하지_않은_signupToken이면_예외_발생() {
            // given
            AuthService authService = createAuthService();
            SignupRequest request = new SignupRequest(
                    "invalid_token", "김고요", "고요한여행자", Gender.MALE,
                    null, null, null, null
            );

            given(jwtTokenProvider.parseSignupToken("invalid_token"))
                    .willThrow(new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN));

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SIGNUP_TOKEN);
        }
    }

    @Nested
    @DisplayName("토큰 갱신 시")
    class Refresh {

        @Test
        void 정상_요청이면_새_토큰_반환() {
            // given
            AuthService authService = createAuthService();
            User user = createActiveUser();

            given(jwtTokenProvider.parseRefreshToken("refresh_token")).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(any(), any())).willReturn("new_access_token");
            given(jwtTokenProvider.createRefreshToken(any())).willReturn("new_refresh_token");
            given(jwtTokenProvider.getAccessTokenExpiration()).willReturn(1800000L);

            // when
            TokenResponse response = authService.refresh("refresh_token");

            // then
            assertThat(response.accessToken()).isEqualTo("new_access_token");
            assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
            assertThat(response.expiresIn()).isEqualTo(1800000L);
        }

        @Test
        void 존재하지_않는_사용자면_예외_발생() {
            // given
            AuthService authService = createAuthService();

            given(jwtTokenProvider.parseRefreshToken("refresh_token")).willReturn(999L);
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh("refresh_token"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 차단된_사용자면_예외_발생() {
            // given
            AuthService authService = createAuthService();
            User blockedUser = org.mockito.Mockito.mock(User.class);
            given(blockedUser.getStatus()).willReturn(UserStatus.BLOCKED);

            given(jwtTokenProvider.parseRefreshToken("refresh_token")).willReturn(1L);
            given(userRepository.findById(1L)).willReturn(Optional.of(blockedUser));

            // when & then
            assertThatThrownBy(() -> authService.refresh("refresh_token"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_BLOCKED);
        }

        @Test
        void 유효하지_않은_토큰이면_예외_발생() {
            // given
            AuthService authService = createAuthService();

            given(jwtTokenProvider.parseRefreshToken("access_token_used_as_refresh"))
                    .willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

            // when & then
            assertThatThrownBy(() -> authService.refresh("access_token_used_as_refresh"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
        }
    }
}
