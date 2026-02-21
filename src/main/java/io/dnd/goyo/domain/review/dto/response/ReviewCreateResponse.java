package io.dnd.goyo.domain.review.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewCreateResponse(
        Long reviewId,
        String representativeImageUrl,
        long reviewOrder
) {

    public static ReviewCreateResponse of(Long reviewId, String representativeImageUrl, long reviewOrder) {
        return new ReviewCreateResponse(reviewId, representativeImageUrl, reviewOrder);
    }
}
