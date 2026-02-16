package io.dnd.goyo.domain.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.domain.user.dto.response.NicknameCheckResponse;
import io.dnd.goyo.domain.user.dto.response.UserProfileResponse;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.service.UserService;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = CustomUserDetails.of(1L, "USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMyProfile {

        @Test
        void 프로필_조회_성공_시_200_반환() throws Exception {
            // given
            UserProfileResponse response = new UserProfileResponse(
                    1L, "김고작", "고작이", LocalDate.of(1995, 3, 15),
                    Gender.MALE, null, true, 1168010100L
            );
            given(userService.getMyProfile(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("김고작"))
                    .andExpect(jsonPath("$.nickname").value("고작이"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/me")
    class Withdraw {

        @Test
        void 회원_탈퇴_성공_시_204_반환() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/users/me")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).withdraw(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/users/nickname/check")
    class CheckNickname {

        @Test
        void 닉네임_빈값이면_400_반환() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/nickname/check")
                            .param("nickname", "")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("nickname"));
        }

        @Test
        void 닉네임_중복_검사_성공() throws Exception {
            // given
            given(userService.checkNickname("새닉네임"))
                    .willReturn(NicknameCheckResponse.from(true));

            // when & then
            mockMvc.perform(get("/api/users/nickname/check")
                            .param("nickname", "새닉네임")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/nickname")
    class UpdateNickname {

        @Test
        void 닉네임_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("nickname", "새닉네임"));

            // when & then
            mockMvc.perform(patch("/api/users/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateNickname(eq(1L), eq("새닉네임"));
        }

        @Test
        void 닉네임_누락_시_400_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("nickname", ""));

            // when & then
            mockMvc.perform(patch("/api/users/me/nickname")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("nickname"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/gender")
    class UpdateGender {

        @Test
        void 성별_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("gender", "FEMALE"));

            // when & then
            mockMvc.perform(patch("/api/users/me/gender")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateGender(eq(1L), eq(Gender.FEMALE));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/birth")
    class UpdateBirth {

        @Test
        void 생년월일_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("birth", "1995-03-15"));

            // when & then
            mockMvc.perform(patch("/api/users/me/birth")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateBirth(eq(1L), eq(LocalDate.of(1995, 3, 15)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/region")
    class UpdateRegion {

        @Test
        void 거주지_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("regionCode", 1111010100L));

            // when & then
            mockMvc.perform(patch("/api/users/me/region")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateRegionCode(eq(1L), eq(1111010100L));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/location-consent")
    class UpdateLocationConsent {

        @Test
        void 위치정보_동의_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("locationConsent", false));

            // when & then
            mockMvc.perform(patch("/api/users/me/location-consent")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateLocationConsent(eq(1L), eq(false));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/profile-image")
    class UpdateProfileImage {

        @Test
        void 프로필_이미지_수정_성공_시_204_반환() throws Exception {
            // given
            String request = objectMapper.writeValueAsString(
                    Map.of("profileImg", "https://example.com/new.jpg"));

            // when & then
            mockMvc.perform(patch("/api/users/me/profile-image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).updateProfileImg(eq(1L), eq("https://example.com/new.jpg"));
        }
    }
}
