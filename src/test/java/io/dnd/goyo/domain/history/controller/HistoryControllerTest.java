package io.dnd.goyo.domain.history.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.dnd.goyo.domain.history.dto.response.HistoryItemResponse;
import io.dnd.goyo.domain.history.service.HistoryService;
import io.dnd.goyo.security.CustomUserDetails;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = CustomUserDetails.of(1L, "USER");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 내_조회_기록_조회_성공_시_200_반환() throws Exception {
        HistoryItemResponse response = new HistoryItemResponse(
                1L, 10L, "테스트 카페", null, "서울시 강남구",
                11110L, "http://localhost:9000/goyo-local/place/image.jpg", 37.5, 127.0, null, null, 0, LocalDateTime.now()
        );
        Page<HistoryItemResponse> page = new PageImpl<>(List.of(response));

        given(historyService.getMyHistories(eq(1L), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/histories/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].historyId").value(1))
                .andExpect(jsonPath("$.content[0].placeId").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void 조회_기록이_없으면_빈_페이지_반환() throws Exception {
        Page<HistoryItemResponse> emptyPage = new PageImpl<>(List.of());

        given(historyService.getMyHistories(eq(1L), any(Pageable.class))).willReturn(emptyPage);

        mockMvc.perform(get("/api/histories/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
