package io.dnd.goyo.domain.place.controller;

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
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import io.dnd.goyo.common.image.dto.request.PresignedUrlRequest;
import io.dnd.goyo.common.image.dto.response.PresignedUrlResponse.PresignedUrlItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaceImageController.class)
class PlaceImageControllerTest {

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
    void 단건_이미지_Presigned_URL_발급_요청_성공() throws Exception {
        // given
        List<String> filenames = List.of("test.jpg");
        PresignedUrlRequest request = new PresignedUrlRequest(filenames);

        PresignedUrlItem item = PresignedUrlItem.of("test.jpg", "http://minio/bucket/place/uuid.jpg", "place/uuid.jpg");
        List<PresignedUrlItem> responseItems = List.of(item);

        given(imageUploadService.createPresignedUrls(eq(ImageType.PLACE), any()))
                .willReturn(responseItems);

        // when & then
        mockMvc.perform(post("/api/places/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[0].filename").value("test.jpg"))
                .andExpect(jsonPath("$.urls[0].url").value("http://minio/bucket/place/uuid.jpg"))
                .andExpect(jsonPath("$.urls[0].objectKey").value("place/uuid.jpg"));
    }

    @Test
    @WithMockUser
    void 다건_이미지_Presigned_URL_발급_요청_성공() throws Exception {
        // given
        List<String> filenames = List.of("img1.jpg", "img2.png");
        PresignedUrlRequest request = new PresignedUrlRequest(filenames);

        List<PresignedUrlItem> responseItems = List.of(
                PresignedUrlItem.of("img1.jpg", "http://minio/bucket/place/uuid1.jpg", "place/uuid1.jpg"),
                PresignedUrlItem.of("img2.png", "http://minio/bucket/place/uuid2.png", "place/uuid2.png")
        );

        given(imageUploadService.createPresignedUrls(eq(ImageType.PLACE), any()))
                .willReturn(responseItems);

        // when & then
        mockMvc.perform(post("/api/places/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls.length()").value(2))
                .andExpect(jsonPath("$.urls[0].filename").value("img1.jpg"))
                .andExpect(jsonPath("$.urls[0].url").value("http://minio/bucket/place/uuid1.jpg"))
                .andExpect(jsonPath("$.urls[0].objectKey").value("place/uuid1.jpg"))
                .andExpect(jsonPath("$.urls[1].filename").value("img2.png"))
                .andExpect(jsonPath("$.urls[1].url").value("http://minio/bucket/place/uuid2.png"))
                .andExpect(jsonPath("$.urls[1].objectKey").value("place/uuid2.png"));
    }

    @Test
    @WithMockUser
    void 파일명_목록_누락_시_400_에러() throws Exception {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest(null);

        // when & then
        mockMvc.perform(post("/api/places/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void 파일명_목록이_비어있으면_400_에러() throws Exception {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest(List.of());

        // when & then
        mockMvc.perform(post("/api/places/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
