package io.dnd.goyo.domain.review.dto.response;

public record ReviewCreateResponse(
        Long reviewId
) {

    public static ReviewCreateResponse from(Long reviewId) {
        return new ReviewCreateResponse(reviewId);
    }
}
