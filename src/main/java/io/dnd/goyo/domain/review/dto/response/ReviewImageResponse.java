package io.dnd.goyo.domain.review.dto.response;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.review.entity.ReviewImage;

public record ReviewImageResponse(
        Long imageId,
        String imageUrl,
        int sequence,
        boolean isPrimary
) {

    public static ReviewImageResponse from(ReviewImage image, FileStorage fileStorage) {
        return new ReviewImageResponse(
                image.getId(),
                fileStorage.generatePublicUrl(image.getImageKey()),
                image.getSequence(),
                image.isPrimary()
        );
    }
}
