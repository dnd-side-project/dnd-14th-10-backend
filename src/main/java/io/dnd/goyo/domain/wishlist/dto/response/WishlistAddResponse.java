package io.dnd.goyo.domain.wishlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "찜 추가 응답")
public record WishlistAddResponse(
        @Schema(description = "생성된 찜 ID", example = "1")
        Long wishlistId
) {

    public static WishlistAddResponse from(Long wishlistId) {
        return new WishlistAddResponse(wishlistId);
    }
}
