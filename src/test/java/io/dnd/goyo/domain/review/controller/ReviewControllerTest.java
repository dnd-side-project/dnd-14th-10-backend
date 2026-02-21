package io.dnd.goyo.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.domain.review.dto.request.ReviewCreateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewCreateResponse;
import io.dnd.goyo.domain.review.dto.request.ReviewUpdateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewDetailResponse;
import io.dnd.goyo.domain.review.dto.response.ReviewRatingStatsResponse;
import io.dnd.goyo.domain.review.dto.response.ReviewTagCountResponse;
import io.dnd.goyo.domain.review.service.ReviewService;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = CustomUserDetails.of(1L, "USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Map<String, Object> createValidCreateRequest() {
        return Map.ofEntries(
                Map.entry("placeId", 10),
                Map.entry("rating", 4.0),
                Map.entry("tagIds", List.of(1, 2)),
                Map.entry("mood", "CALM"),
                Map.entry("spaceSize", "MEDIUM"),
                Map.entry("outletScore", "MANY"),
                Map.entry("crowdStatus", "NORMAL"),
                Map.entry("content", "좋은 카페입니다")
        );
    }

    private Map<String, Object> createValidUpdateRequest() {
        return Map.ofEntries(
                Map.entry("rating", 5.0),
                Map.entry("tagIds", List.of(2, 3)),
                Map.entry("mood", "SILENT"),
                Map.entry("spaceSize", "LARGE"),
                Map.entry("outletScore", "FEW"),
                Map.entry("crowdStatus", "RELAX"),
                Map.entry("content", "수정된 내용")
        );
    }

    @Test
    void 리뷰_작성_성공_시_201_반환() throws Exception {
        given(reviewService.createReview(eq(1L), any(ReviewCreateRequest.class)))
                .willReturn(ReviewCreateResponse.of(100L, null, 1L));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidCreateRequest()))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(100))
                .andExpect(jsonPath("$.representativeImageUrl").doesNotExist())
                .andExpect(jsonPath("$.reviewOrder").value(1));

        verify(reviewService).createReview(eq(1L), any(ReviewCreateRequest.class));
    }

    @Test
    @WithMockUser
    void 평점_누락_시_400_반환() throws Exception {
        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.remove("rating");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("rating"));
    }

    @Test
    @WithMockUser
    void 태그_1개만_선택_시_400_반환() throws Exception {
        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.put("tagIds", List.of(1));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("tagIds"));
    }

    @Test
    @WithMockUser
    void 태그_6개_선택_시_400_반환() throws Exception {
        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.put("tagIds", List.of(1, 2, 3, 4, 5, 6));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("tagIds"));
    }

    @Test
    void 리뷰_조회_성공_시_200_반환() throws Exception {
        ReviewDetailResponse response = new ReviewDetailResponse(
                1L, 10L, 1L, "테스터", "profile.jpg",
                4.0, null, null, null, null,
                "좋은 카페입니다", List.of(), List.of(), null, null
        );
        given(reviewService.getReview(1L)).willReturn(response);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.rating").value(4.0));
    }

    @Test
    void 리뷰_수정_성공_시_204_반환() throws Exception {
        doNothing().when(reviewService).updateReview(eq(1L), eq(1L), any(ReviewUpdateRequest.class));

        mockMvc.perform(patch("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidUpdateRequest()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reviewService).updateReview(eq(1L), eq(1L), any(ReviewUpdateRequest.class));
    }

    @Test
    void 리뷰_삭제_성공_시_204_반환() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/reviews/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1L, 1L);
    }

    @Test
    void 내_리뷰_목록_조회_성공_시_200_반환() throws Exception {
        ReviewDetailResponse response = new ReviewDetailResponse(
                1L, 10L, 1L, "테스터", "profile.jpg",
                4.0, null, null, null, null,
                "좋은 카페입니다", List.of(), List.of(), null, null
        );
        Page<ReviewDetailResponse> page = new PageImpl<>(List.of(response));

        given(reviewService.getMyReviews(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/reviews/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reviewId").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(4.0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void 평점이_0점5_단위가_아니면_400_반환() throws Exception {
        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.put("rating", 1.3);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void 중복_태그_ID_포함_시_400_반환() throws Exception {
        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.put("tagIds", List.of(1, 1, 2));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("tagIds"));
    }

    @Test
    @WithMockUser
    void 이미지_7개_등록_시_400_반환() throws Exception {
        List<Map<String, Object>> images = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            images.add(Map.of("imageKey", "review/img" + i + ".jpg", "sequence", i, "isPrimary", i == 0));
        }

        Map<String, Object> request = new HashMap<>(createValidCreateRequest());
        request.put("images", images);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 리뷰_태그_통계_조회_성공_시_200_반환() throws Exception {
        List<ReviewTagCountResponse> response = List.of(
                new ReviewTagCountResponse(1L, "QUIET", "조용한", 5),
                new ReviewTagCountResponse(2L, "COZY", "아늑한", 3)
        );
        given(reviewService.getReviewTagStatsByPlace(10L)).willReturn(response);

        mockMvc.perform(get("/api/places/10/reviews/tag-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tagId").value(1))
                .andExpect(jsonPath("$[0].code").value("QUIET"))
                .andExpect(jsonPath("$[0].name").value("조용한"))
                .andExpect(jsonPath("$[0].count").value(5))
                .andExpect(jsonPath("$[1].tagId").value(2))
                .andExpect(jsonPath("$[1].count").value(3));
    }

    @Test
    void 리뷰_별점_통계_조회_성공_시_200_반환() throws Exception {
        ReviewRatingStatsResponse response = new ReviewRatingStatsResponse(4.2, 7);
        given(reviewService.getReviewRatingStatsByPlace(10L)).willReturn(response);

        mockMvc.perform(get("/api/places/10/reviews/rating-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.2))
                .andExpect(jsonPath("$.reviewCount").value(7));
    }
}
