package io.dnd.goyo.domain.review.dto.request;

import io.dnd.goyo.common.validation.Primaryable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewImageRequest(
        @NotBlank(message = "이미지 키는 필수입니다")
        String imageKey,

        @NotNull(message = "이미지 순서는 필수입니다")
        Integer sequence,

        boolean isPrimary
) implements Primaryable {
}
