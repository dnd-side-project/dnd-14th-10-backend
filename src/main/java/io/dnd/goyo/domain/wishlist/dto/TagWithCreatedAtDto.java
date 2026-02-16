package io.dnd.goyo.domain.wishlist.dto;

import java.time.LocalDateTime;

public record TagWithCreatedAtDto(
        Long tagId,
        LocalDateTime createdAt
) {
}