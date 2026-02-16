package io.dnd.goyo.domain.review.dto.response;

import io.dnd.goyo.domain.review.entity.ReviewImage;

public record ReviewImageResponse(
        Long imageId,
        String imageUrl,
        int sequence
) {

    public static ReviewImageResponse from(ReviewImage image) {
        return new ReviewImageResponse(image.getId(), image.getImageUrl(), image.getSequence());
    }
}
