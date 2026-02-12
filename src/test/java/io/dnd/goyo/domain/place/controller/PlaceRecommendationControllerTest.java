package io.dnd.goyo.domain.place.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.dnd.goyo.common.auth.security.UserPrincipal;
import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.service.PlaceRecommendationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaceRecommendationController.class)
class PlaceRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceRecommendationService placeRecommendationService;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(1L);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 신규_공간_조회_성공_시_200_반환() throws Exception {
        // given
        PlaceSummaryResponse response = new PlaceSummaryResponse(
                1L, "테스트 카페", PlaceCategory.CAFE, "청계천로 101",
                11010, "images/test.jpg", 37.566, 126.978,
                Mood.CALM, SpaceSize.MEDIUM, false
        );

        given(placeRecommendationService.getNewPlaces(
                eq(1L), eq(126.978), eq(37.566), eq(11010), eq(PlaceCategory.CAFE), isNull()
        )).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/places/recommendations/new")
                        .param("longitude", "126.978")
                        .param("latitude", "37.566")
                        .param("regionCode", "11010")
                        .param("category", "CAFE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("테스트 카페"))
                .andExpect(jsonPath("$[0].category").value("CAFE"))
                .andExpect(jsonPath("$[0].isWished").value(false));
    }

    @Test
    void 결과_없으면_빈_리스트_반환() throws Exception {
        // given
        given(placeRecommendationService.getNewPlaces(
                eq(1L), eq(126.978), eq(37.566), eq(11010), eq(PlaceCategory.CAFE), isNull()
        )).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/places/recommendations/new")
                        .param("longitude", "126.978")
                        .param("latitude", "37.566")
                        .param("regionCode", "11010")
                        .param("category", "CAFE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void 필수_파라미터_누락_시_400_반환() throws Exception {
        // when & then
        mockMvc.perform(get("/api/places/recommendations/new")
                        .param("longitude", "126.978"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 커스텀_반경_전달() throws Exception {
        // given
        given(placeRecommendationService.getNewPlaces(
                eq(1L), eq(126.978), eq(37.566), eq(11010), eq(PlaceCategory.CAFE), eq(5000)
        )).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/places/recommendations/new")
                        .param("longitude", "126.978")
                        .param("latitude", "37.566")
                        .param("regionCode", "11010")
                        .param("category", "CAFE")
                        .param("radiusMeters", "5000"))
                .andExpect(status().isOk());
    }
}
