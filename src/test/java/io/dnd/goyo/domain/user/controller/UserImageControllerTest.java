package io.dnd.goyo.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.common.image.ImageType;
import io.dnd.goyo.common.image.ImageUploadService;
import io.dnd.goyo.common.image.dto.request.PresignedUrlRequest;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserImageController.class)
class UserImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/users/images/presigned-url")
    class GetPresignedUrl {

        @Test
        @WithMockUser
        @DisplayName("프로필 이미지 Presigned URL 발급 요청 성공")
        void 프로필_이미지_Presigned_URL_발급_요청_성공() throws Exception {
            // given
            List<String> filenames = List.of("profile.jpg");
            PresignedUrlRequest request = new PresignedUrlRequest(filenames);

            PresignedUrlItem item = PresignedUrlItem.of("profile.jpg", "http://minio/bucket/user/uuid.jpg", "user/uuid.jpg");
            List<PresignedUrlItem> responseItems = List.of(item);

            given(imageUploadService.createPresignedUrls(eq(ImageType.USER), any()))
                    .willReturn(responseItems);

            // when & then
            mockMvc.perform(post("/api/users/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.urls[0].filename").value("profile.jpg"))
                    .andExpect(jsonPath("$.urls[0].url").value("http://minio/bucket/user/uuid.jpg"))
                    .andExpect(jsonPath("$.urls[0].objectKey").value("user/uuid.jpg"));
        }

        @Test
        @WithMockUser
        @DisplayName("파일명 목록 누락 시 400 에러")
        void 파일명_목록_누락_시_400_에러() throws Exception {
            // given
            PresignedUrlRequest request = new PresignedUrlRequest(null);

            // when & then
            mockMvc.perform(post("/api/users/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("파일명 목록이 비어있으면 400 에러")
        void 파일명_목록이_비어있으면_400_에러() throws Exception {
            // given
            PresignedUrlRequest request = new PresignedUrlRequest(List.of());

            // when & then
            mockMvc.perform(post("/api/users/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("파일명 2개 이상이면 400 에러")
        void 파일명_2개_이상이면_400_에러() throws Exception {
            // given
            PresignedUrlRequest request = new PresignedUrlRequest(List.of("a.jpg", "b.jpg"));

            // when & then
            mockMvc.perform(post("/api/users/images/presigned-url")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("USER_003"));
        }
    }
}
