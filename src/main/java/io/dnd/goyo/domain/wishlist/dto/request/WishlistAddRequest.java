package io.dnd.goyo.domain.wishlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "찜 추가 요청")
public record WishlistAddRequest(
        @Schema(description = "장소 ID", example = "1")
        @NotNull(message = "장소 ID는 필수입니다") Long placeId
) {
}
