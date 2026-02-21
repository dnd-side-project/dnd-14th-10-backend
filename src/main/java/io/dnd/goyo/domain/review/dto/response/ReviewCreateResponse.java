package io.dnd.goyo.domain.review.dto.response;

public record ReviewCreateResponse(
        Long reviewId,
        String representativeImageUrl,
        long reviewOrder
) {

    public static ReviewCreateResponse of(Long reviewId, String representativeImageUrl, long reviewOrder) {
        return new ReviewCreateResponse(reviewId, representativeImageUrl, reviewOrder);
    }
}
