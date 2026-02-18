package io.dnd.goyo.domain.history.event;

public record PlaceViewedEvent(
        Long userId,
        Long placeId
) {}
