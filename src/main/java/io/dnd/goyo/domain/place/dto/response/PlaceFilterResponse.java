package io.dnd.goyo.domain.place.dto.response;

import java.util.List;

public record PlaceFilterResponse(
        List<PlaceMapItemResponse> places,
        Long lastPlaceId,
        boolean hasNext
) {
    public static PlaceFilterResponse empty() {
        return new PlaceFilterResponse(List.of(), null, false);
    }
}
