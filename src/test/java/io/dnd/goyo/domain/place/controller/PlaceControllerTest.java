package io.dnd.goyo.domain.place.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.common.auth.security.UserPrincipal;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.service.PlaceService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(1L);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Map<String, Object> createValidRequest() {
        return Map.ofEntries(
                Map.entry("name", "테스트 카페"),
                Map.entry("category", "CAFE"),
                Map.entry("latitude", 37.5),
                Map.entry("longitude", 127.0),
                Map.entry("regionCode", 11111),
                Map.entry("addressDetail", "청계천로 100"),
                Map.entry("outletScore", "MANY"),
                Map.entry("spaceSize", "LARGE"),
                Map.entry("crowdStatus", "RELAX"),
                Map.entry("mood", "CALM"),
                Map.entry("images", List.of(
                        Map.of("imageKey", "place/uuid.jpg", "sequence", 0, "isRepresentative", true),
                        Map.of("imageKey", "place/uuid2.jpg", "sequence", 1, "isRepresentative", false)
                ))
        );
    }

    @Test
    void 장소_등록_성공_시_201_반환() throws Exception {
        // given
        given(placeService.registerPlace(eq(1L), any(PlaceRegisterRequest.class)))
                .willReturn(100L);
        String request = objectMapper.writeValueAsString(createValidRequest());

        // when & then
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placeId").value(100));

        verify(placeService).registerPlace(eq(1L), any(PlaceRegisterRequest.class));
    }

    @Test
    @WithMockUser
    void 장소_이름_누락_시_400_반환() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>(createValidRequest());
        request.remove("name");

        // when & then
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @WithMockUser
    void 카테고리_누락_시_400_반환() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>(createValidRequest());
        request.remove("category");

        // when & then
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("category"));
    }

    @Test
    @WithMockUser
    void 이미지_누락_시_400_반환() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>(createValidRequest());
        request.remove("images");

        // when & then
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("images"));
    }

    @Test
    @WithMockUser
    void 이미지_빈_리스트_시_400_반환() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>(createValidRequest());
        request.put("images", List.of());

        // when & then
        mockMvc.perform(post("/api/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("images"));
    }
}
