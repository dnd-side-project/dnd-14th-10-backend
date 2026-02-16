package io.dnd.goyo.domain.review.dto;

import java.time.LocalDateTime;

public record TagWithCreatedAtDto(
        Long tagId,
        LocalDateTime createdAt
) {
}