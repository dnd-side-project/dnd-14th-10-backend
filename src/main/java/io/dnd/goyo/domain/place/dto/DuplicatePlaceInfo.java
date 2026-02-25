package io.dnd.goyo.domain.place.dto;

public record DuplicatePlaceInfo(
        Long existingPlaceId,
        String existingPlaceName
) {
}
