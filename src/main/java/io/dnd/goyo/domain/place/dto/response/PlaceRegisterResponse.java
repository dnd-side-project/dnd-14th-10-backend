package io.dnd.goyo.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PlaceRegisterResponse(
        @Schema(description = "생성된 공간 ID", example = "1")
        Long placeId
) {
    public static PlaceRegisterResponse from(Long id) {
        return new PlaceRegisterResponse(id);
    }
}
