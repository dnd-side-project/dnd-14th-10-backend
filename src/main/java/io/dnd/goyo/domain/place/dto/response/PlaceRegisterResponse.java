package io.dnd.goyo.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PlaceRegisterResponse(
        @Schema(description = "생성된 장소 ID", example = "1")
        Long id
) {
    public static PlaceRegisterResponse from(Long id) {
        return new PlaceRegisterResponse(id);
    }
}
