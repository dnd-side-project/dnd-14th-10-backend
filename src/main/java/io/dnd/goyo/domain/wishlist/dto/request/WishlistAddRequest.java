package io.dnd.goyo.domain.wishlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "찜 추가 요청")
public record WishlistAddRequest(
        @Schema(description = "공간 ID", example = "1")
        @NotNull(message = "공간 ID는 필수입니다") Long placeId
) {
}
