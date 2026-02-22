package io.dnd.goyo.domain.review.dto.response;

public record ReviewTagCountResponse(
        Long tagId,
        String code,
        String name,
        long count
) {
}
