package io.dnd.goyo.domain.place.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceUpdateRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import io.dnd.goyo.domain.place.service.PlaceService;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
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

@WebMvcTest(PlaceController.class)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = CustomUserDetails.of(1L, "USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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

    @Nested
    @DisplayName("POST /api/places")
    class RegisterPlace {

        @Test
        void 공간_등록_성공_시_201_반환() throws Exception {
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
        void 공간_이름_누락_시_400_반환() throws Exception {
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

    @Nested
    @DisplayName("GET /api/places/{placeId}")
    class GetPlaceDetail {

        @Test
        void 회원_찜한_공간_조회_성공() throws Exception {
            // given
            Long placeId = 1L;
            PlaceDetailResponse response = createPlaceDetailResponse(placeId, true);
            given(placeService.getPlaceDetail(1L, placeId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(placeId))
                    .andExpect(jsonPath("$.name").value("테스트 카페"))
                    .andExpect(jsonPath("$.category").value("CAFE"))
                    .andExpect(jsonPath("$.isWished").value(true))
                    .andExpect(jsonPath("$.images[0]").value("http://localhost:9000/goyo-local/place/image1.jpg"))
                    .andExpect(jsonPath("$.images[1]").value("http://localhost:9000/goyo-local/place/image2.jpg"));

            verify(placeService).getPlaceDetail(1L, placeId);
        }

        @Test
        void 회원_찜하지_않은_공간_조회_성공() throws Exception {
            // given
            Long placeId = 2L;
            PlaceDetailResponse response = createPlaceDetailResponse(placeId, false);
            given(placeService.getPlaceDetail(1L, placeId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(placeId))
                    .andExpect(jsonPath("$.isWished").value(false));

            verify(placeService).getPlaceDetail(1L, placeId);
        }

        @Test
        void 존재하지_않는_공간_조회_실패() throws Exception {
            // given
            Long placeId = 999L;
            given(placeService.getPlaceDetail(1L, placeId))
                    .willThrow(new BusinessException(ErrorCode.PLACE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(placeService).getPlaceDetail(1L, placeId);
        }

        @Test
        void 이미지_URL_변환_확인() throws Exception {
            // given
            Long placeId = 4L;
            PlaceDetailResponse response = createPlaceDetailResponse(placeId, false);
            given(placeService.getPlaceDetail(1L, placeId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images").isArray())
                    .andExpect(jsonPath("$.images[0]").value("http://localhost:9000/goyo-local/place/image1.jpg"))
                    .andExpect(jsonPath("$.images[1]").value("http://localhost:9000/goyo-local/place/image2.jpg"));
        }

        private PlaceDetailResponse createPlaceDetailResponse(Long placeId, boolean isWished) {
            return new PlaceDetailResponse(
                    placeId,
                    "테스트 카페",
                    PlaceCategory.CAFE,
                    "서울시 강남구 테헤란로 123",
                    37.5,
                    127.0,
                    List.of(
                            "http://localhost:9000/goyo-local/place/image1.jpg",
                            "http://localhost:9000/goyo-local/place/image2.jpg"
                    ),
                    4.5,
                    10,
                    SpaceSize.LARGE,
                    Mood.CALM,
                    OutletScore.MANY,
                    CrowdStatus.RELAX,
                    LocalTime.of(9, 0),
                    LocalTime.of(22, 0),
                    1,
                    "내부/남녀공용",
                    isWished
            );
        }
    }

    @Nested
    @DisplayName("PATCH /api/places/{placeId}")
    class UpdatePlace {

        private Map<String, Object> createValidUpdateRequest() {
            return Map.of(
                    "name", "수정된 카페",
                    "images", List.of(
                            Map.of("imageKey", "place/uuid.jpg", "sequence", 0, "isRepresentative", true)
                    )
            );
        }

        @Test
        void 공간_수정_성공_시_204_반환() throws Exception {
            // given
            Long placeId = 1L;
            String request = objectMapper.writeValueAsString(createValidUpdateRequest());

            // when & then
            mockMvc.perform(patch("/api/places/{placeId}", placeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(placeService).updatePlace(eq(1L), eq(placeId), any(PlaceUpdateRequest.class));
        }

        @Test
        void 존재하지_않는_공간_수정_시_404_반환() throws Exception {
            // given
            Long placeId = 999L;
            doThrow(new BusinessException(ErrorCode.PLACE_NOT_FOUND))
                    .when(placeService).updatePlace(eq(1L), eq(placeId), any(PlaceUpdateRequest.class));
            String request = objectMapper.writeValueAsString(createValidUpdateRequest());

            // when & then
            mockMvc.perform(patch("/api/places/{placeId}", placeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 다른_사용자가_수정_시도_시_403_반환() throws Exception {
            // given
            Long placeId = 1L;
            doThrow(new BusinessException(ErrorCode.FORBIDDEN))
                    .when(placeService).updatePlace(eq(1L), eq(placeId), any(PlaceUpdateRequest.class));
            String request = objectMapper.writeValueAsString(createValidUpdateRequest());

            // when & then
            mockMvc.perform(patch("/api/places/{placeId}", placeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void 이미지_빈_리스트_시_400_반환() throws Exception {
            // given
            Long placeId = 1L;
            Map<String, Object> request = new HashMap<>();
            request.put("name", "수정된 카페");
            request.put("images", List.of());

            // when & then
            mockMvc.perform(patch("/api/places/{placeId}", placeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("images"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/places/{placeId}")
    class DeletePlace {

        @Test
        void 공간_삭제_성공_시_204_반환() throws Exception {
            // given
            Long placeId = 1L;

            // when & then
            mockMvc.perform(delete("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(placeService).deletePlace(1L, placeId);
        }

        @Test
        void 존재하지_않는_공간_삭제_시_404_반환() throws Exception {
            // given
            Long placeId = 999L;
            doThrow(new BusinessException(ErrorCode.PLACE_NOT_FOUND))
                    .when(placeService).deletePlace(1L, placeId);

            // when & then
            mockMvc.perform(delete("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(placeService).deletePlace(1L, placeId);
        }

        @Test
        void 다른_사용자가_삭제_시도_시_403_반환() throws Exception {
            // given
            Long placeId = 1L;
            doThrow(new BusinessException(ErrorCode.FORBIDDEN))
                    .when(placeService).deletePlace(1L, placeId);

            // when & then
            mockMvc.perform(delete("/api/places/{placeId}", placeId)
                            .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(placeService).deletePlace(1L, placeId);
        }
    }
}
