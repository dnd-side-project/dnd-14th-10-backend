package io.dnd.goyo.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewImageRequest(
        @NotBlank(message = "이미지 URL은 필수입니다")
        String imageUrl,

        @NotNull(message = "이미지 순서는 필수입니다")
        Integer sequence
) {
}
