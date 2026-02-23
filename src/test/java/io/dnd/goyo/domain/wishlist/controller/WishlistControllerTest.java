package io.dnd.goyo.domain.wishlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.wishlist.dto.request.WishlistAddRequest;
import io.dnd.goyo.domain.wishlist.dto.response.WishCountResponse;
import io.dnd.goyo.domain.wishlist.dto.response.WishlistItemResponse;
import io.dnd.goyo.domain.wishlist.enums.WishlistSortType;
import io.dnd.goyo.domain.wishlist.service.WishlistService;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = CustomUserDetails.of(1L, "USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void 찜_추가_성공_시_201_반환() throws Exception {
        given(wishlistService.addWishlist(eq(1L), any(WishlistAddRequest.class)))
                .willReturn(100L);

        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("placeId", 10)))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.wishlistId").value(100));

        verify(wishlistService).addWishlist(eq(1L), any(WishlistAddRequest.class));
    }

    @Test
    void placeId_누락_시_400_반환() throws Exception {
        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 내_찜_목록_조회_성공_시_200_반환() throws Exception {
        WishlistItemResponse response = new WishlistItemResponse(
                1L, 10L, "테스트 카페", "http://localhost:9000/goyo-local/place/image.jpg",
                Mood.CALM, SpaceSize.MEDIUM, 5, true, null
        );
        Page<WishlistItemResponse> page = new PageImpl<>(List.of(response));

        given(wishlistService.getMyWishlists(eq(1L), any(Pageable.class), any(WishlistSortType.class))).willReturn(page);

        mockMvc.perform(get("/api/wishlists/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].wishlistId").value(1))
                .andExpect(jsonPath("$.content[0].placeId").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void 찜_삭제_성공_시_204_반환() throws Exception {
        doNothing().when(wishlistService).removeWishlist(1L, 10L);

        mockMvc.perform(delete("/api/wishlists/places/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(wishlistService).removeWishlist(1L, 10L);
    }

    @Test
    void 찜_수_조회_성공_시_200_반환() throws Exception {
        given(wishlistService.getWishCount(10L))
                .willReturn(WishCountResponse.of(10L, 5));

        mockMvc.perform(get("/api/places/10/wish-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId").value(10))
                .andExpect(jsonPath("$.wishCount").value(5));
    }
}
