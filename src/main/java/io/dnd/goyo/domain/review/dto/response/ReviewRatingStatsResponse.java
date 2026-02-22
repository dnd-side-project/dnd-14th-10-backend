package io.dnd.goyo.domain.review.dto.response;

public record ReviewRatingStatsResponse(
        double averageRating,
        int reviewCount
) {
}
