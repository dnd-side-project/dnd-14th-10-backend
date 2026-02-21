package io.dnd.goyo.domain.review.controller;

import io.dnd.goyo.domain.review.dto.request.ReviewCreateRequest;
import io.dnd.goyo.domain.review.dto.request.ReviewUpdateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewCreateResponse;
import io.dnd.goyo.domain.review.dto.response.ReviewDetailResponse;
import io.dnd.goyo.domain.review.service.ReviewService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "공간에 대한 리뷰를 작성합니다.")
    @PostMapping("/reviews")
    public ResponseEntity<ReviewCreateResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewCreateResponse response = reviewService.createReview(userDetails.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "리뷰 단건 조회", description = "리뷰 상세 정보를 조회합니다.")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> getReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReview(reviewId));
    }

    @Operation(summary = "공간별 리뷰 목록 조회", description = "공간에 달린 리뷰 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/places/{placeId}/reviews")
    public ResponseEntity<Page<ReviewDetailResponse>> getReviewsByPlace(
            @PathVariable Long placeId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByPlace(placeId, pageable));
    }

    @Operation(summary = "내 리뷰 목록 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/reviews/me")
    public ResponseEntity<Page<ReviewDetailResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.getMyReviews(userDetails.userId(), pageable));
    }

    @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        reviewService.updateReview(userDetails.userId(), reviewId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userDetails.userId(), reviewId);
        return ResponseEntity.noContent().build();
    }
}
