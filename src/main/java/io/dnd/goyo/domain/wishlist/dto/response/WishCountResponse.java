package io.dnd.goyo.domain.wishlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장소 찜 수 응답")
public record WishCountResponse(
        @Schema(description = "장소 ID", example = "10")
        Long placeId,
        @Schema(description = "찜 수", example = "42")
        int wishCount
) {

    public static WishCountResponse of(Long placeId, int wishCount) {
        return new WishCountResponse(placeId, wishCount);
    }
}
