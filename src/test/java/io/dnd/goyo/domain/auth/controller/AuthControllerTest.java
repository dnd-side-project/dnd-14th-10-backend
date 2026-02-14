package io.dnd.goyo.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.LoginResponse;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.TokenResponse;
import io.dnd.goyo.domain.auth.dto.response.UserInfoResponse;
import io.dnd.goyo.domain.auth.service.AuthService;
import io.dnd.goyo.domain.auth.service.oauth.OAuthUserInfo;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/auth/oauth/{provider}")
    class OAuthLogin {

        @Test
        @WithMockUser
        void 기존_사용자_로그인_성공_시_200_반환() throws Exception {
            // given
            OAuthLoginResponse response = OAuthLoginResponse.forExistingUser(
                    "access_token", "refresh_token", 1800000L,
                    new UserInfoResponse(1L, "고요한여행자", null)
            );
            given(authService.oauthLogin(eq(Provider.KAKAO), any(String.class))).willReturn(response);
            String request = objectMapper.writeValueAsString(Map.of("code", "auth_code"));

            // when & then
            mockMvc.perform(post("/api/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isNewUser").value(false))
                    .andExpect(jsonPath("$.accessToken").value("access_token"))
                    .andExpect(jsonPath("$.user.nickname").value("고요한여행자"));
        }

        @Test
        @WithMockUser
        void 신규_사용자_로그인_시_200_반환() throws Exception {
            // given
            OAuthUserInfo userInfo = new OAuthUserInfo(Provider.KAKAO, "kakao_new", "새유저", "profile.jpg");
            OAuthLoginResponse response = OAuthLoginResponse.forNewUser("signup_token", userInfo);
            given(authService.oauthLogin(eq(Provider.KAKAO), any(String.class))).willReturn(response);
            String request = objectMapper.writeValueAsString(Map.of("code", "auth_code"));

            // when & then
            mockMvc.perform(post("/api/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isNewUser").value(true))
                    .andExpect(jsonPath("$.signupToken").value("signup_token"));
        }

        @Test
        @WithMockUser
        void 인가코드_누락_시_400_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(Map.of());

            // when & then
            mockMvc.perform(post("/api/auth/oauth/kakao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("code"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/signup")
    class Signup {

        private Map<String, Object> createValidSignupRequest() {
            return Map.of(
                    "signupToken", "signup_token",
                    "name", "김고요",
                    "nickname", "고요한여행자",
                    "gender", "MALE",
                    "birth", "1995-03-15",
                    "locationConsent", true,
                    "regionCode", 11680
            );
        }

        @Test
        @WithMockUser
        void 회원가입_성공_시_201_반환() throws Exception {
            // given
            LoginResponse response = new LoginResponse(
                    "access_token", "refresh_token", 1800000L,
                    new UserInfoResponse(1L, "고요한여행자", null)
            );
            given(authService.signup(any(SignupRequest.class))).willReturn(response);
            String request = objectMapper.writeValueAsString(createValidSignupRequest());

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("access_token"))
                    .andExpect(jsonPath("$.user.nickname").value("고요한여행자"));
        }

        @Test
        @WithMockUser
        void signupToken_누락_시_400_반환() throws Exception {
            // given
            Map<String, Object> request = new HashMap<>(createValidSignupRequest());
            request.remove("signupToken");

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("signupToken"));
        }

        @Test
        @WithMockUser
        void 이름_누락_시_400_반환() throws Exception {
            // given
            Map<String, Object> request = new HashMap<>(createValidSignupRequest());
            request.remove("name");

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("name"));
        }

        @Test
        @WithMockUser
        void 닉네임_누락_시_400_반환() throws Exception {
            // given
            Map<String, Object> request = new HashMap<>(createValidSignupRequest());
            request.remove("nickname");

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("nickname"));
        }

        @Test
        @WithMockUser
        void 성별_누락_시_400_반환() throws Exception {
            // given
            Map<String, Object> request = new HashMap<>(createValidSignupRequest());
            request.remove("gender");

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("gender"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @WithMockUser
        void 토큰_갱신_성공_시_200_반환() throws Exception {
            // given
            TokenResponse response = new TokenResponse("new_access_token", "new_refresh_token", 1800000L);
            given(authService.refresh(any(String.class))).willReturn(response);
            String request = objectMapper.writeValueAsString(Map.of("refreshToken", "refresh_token"));

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                    .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));
        }

        @Test
        @WithMockUser
        void refreshToken_누락_시_400_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(Map.of());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
        }
    }
}
