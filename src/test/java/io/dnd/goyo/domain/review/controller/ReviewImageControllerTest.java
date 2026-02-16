package io.dnd.goyo.domain.review.controller;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewImageController.class)
class ReviewImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    void 이미지_Presigned_URL_발급_성공() throws Exception {
        List<String> filenames = List.of("review1.jpg");
        PresignedUrlRequest request = new PresignedUrlRequest(filenames);
        PresignedUrlItem item = PresignedUrlItem.of("review1.jpg",
                "http://minio/bucket/review/uuid.jpg", "review/uuid.jpg");

        given(imageUploadService.createPresignedUrls(eq(ImageType.REVIEW), any()))
                .willReturn(List.of(item));

        mockMvc.perform(post("/api/reviews/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0].filename").value("review1.jpg"))
                .andExpect(jsonPath("$.urls[0].url").value("http://minio/bucket/review/uuid.jpg"))
                .andExpect(jsonPath("$.urls[0].objectKey").value("review/uuid.jpg"));
    }

    @Test
    @WithMockUser
    void 파일명_목록_누락_시_400() throws Exception {
        PresignedUrlRequest request = new PresignedUrlRequest(null);

        mockMvc.perform(post("/api/reviews/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
