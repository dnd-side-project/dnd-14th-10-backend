package io.dnd.goyo.domain.badge.controller;

import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse;
import io.dnd.goyo.domain.badge.service.BadgeService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Badge", description = "뱃지 관련 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(summary = "뱃지 진행상황 조회", description = "로그인한 사용자의 뱃지 진행상황을 조회합니다.")
    @GetMapping("/progress")
    public ResponseEntity<BadgeProgressResponse> getBadgeProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(badgeService.getBadgeProgress(userDetails.userId()));
    }
}
