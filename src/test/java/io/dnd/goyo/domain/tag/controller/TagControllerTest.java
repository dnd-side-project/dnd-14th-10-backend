package io.dnd.goyo.domain.tag.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.dnd.goyo.domain.tag.dto.response.TagResponse;
import io.dnd.goyo.domain.tag.service.TagService;
import io.dnd.goyo.security.jwt.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    void 태그_목록_조회_성공() throws Exception {
        List<TagResponse> tags = List.of(
                new TagResponse(1L, "CLEAN", "청결해요"),
                new TagResponse(2L, "FOCUS", "집중하기 좋아요")
        );
        given(tagService.getTags()).willReturn(tags);

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("CLEAN"))
                .andExpect(jsonPath("$[0].name").value("청결해요"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("FOCUS"))
                .andExpect(jsonPath("$[1].name").value("집중하기 좋아요"));
    }
}
