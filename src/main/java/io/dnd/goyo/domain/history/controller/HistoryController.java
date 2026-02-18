package io.dnd.goyo.domain.history.controller;

import io.dnd.goyo.domain.history.dto.response.HistoryItemResponse;
import io.dnd.goyo.domain.history.service.HistoryService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "History", description = "조회 기록 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "내 조회 기록 조회", description = "로그인한 사용자의 공간 조회 기록을 페이지네이션으로 조회합니다.")
    @GetMapping("/histories/me")
    public ResponseEntity<Page<HistoryItemResponse>> getMyHistories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "viewedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(historyService.getMyHistories(userDetails.userId(), pageable));
    }
}
