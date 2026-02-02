package io.dnd.goyo.domain.place.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.dnd.goyo.common.image.ImageType;
import io.dnd.goyo.common.image.ImageUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaceImageController.class)
class PlaceImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageUploadService imageUploadService;

    @Test
    @WithMockUser
    void Presigned_URL_발급_요청_성공() throws Exception {
        // given
        String filename = "test.jpg";
        String expectedUrl = "http://minio/bucket/place/uuid.jpg";
        
        given(imageUploadService.createPresignedUrl(eq(ImageType.PLACE), eq(filename)))
                .willReturn(expectedUrl);

        // when & then
        mockMvc.perform(get("/api/places/images/presigned-url")
                        .param("filename", filename)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }

    @Test
    @WithMockUser
    void 파일명_파라미터_누락_시_400_에러() throws Exception {
        // when & then
        mockMvc.perform(get("/api/places/images/presigned-url")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
