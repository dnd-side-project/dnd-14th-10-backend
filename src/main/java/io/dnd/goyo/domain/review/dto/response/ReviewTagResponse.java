package io.dnd.goyo.domain.review.dto.response;

import io.dnd.goyo.domain.review.entity.ReviewTag;

public record ReviewTagResponse(
        Long tagId,
        String code,
        String name
) {

    public static ReviewTagResponse from(ReviewTag reviewTag) {
        return new ReviewTagResponse(
                reviewTag.getTag().getId(),
                reviewTag.getTag().getCode(),
                reviewTag.getTag().getName()
        );
    }
}
