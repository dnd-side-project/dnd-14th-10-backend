package io.dnd.goyo.domain.place.dto.request;

import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceImageRequest(
        @Schema(description = "이미지 업로드 키 (objectKey)", example = "place/uuid.jpg")
        @NotBlank(message = "이미지 키는 필수입니다")
        String imageKey,

        @Schema(description = "노출 순서 (0부터 시작)", example = "0")
        @NotNull(message = "순서는 필수입니다")
        Integer sequence,

        @Schema(description = "대표 사진 여부", example = "true")
        @NotNull(message = "대표 사진 여부는 필수입니다")
        Boolean isRepresentative
) {
    public PlaceImage toEntity() {
        return PlaceImage.of(imageKey, Boolean.TRUE.equals(isRepresentative), sequence);
    }
}
