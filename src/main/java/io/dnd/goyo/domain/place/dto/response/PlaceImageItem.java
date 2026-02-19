package io.dnd.goyo.domain.place.dto.response;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;

@Schema(description = "공간 이미지")
public record PlaceImageItem(
        @Schema(description = "이미지 URL", example = "https://...")
        String url,

        @Schema(description = "순서", example = "0")
        int sequence,

        @Schema(description = "대표 이미지 여부", example = "true")
        boolean representativeFlag
) {
    public static PlaceImageItem of(PlaceImage image, FileStorage fileStorage) {
        return new PlaceImageItem(
                fileStorage.generatePublicUrl(image.getImageKey()),
                image.getSequence(),
                image.isRepresentativeFlag()
        );
    }

    public static List<PlaceImageItem> listOf(List<PlaceImage> images, FileStorage fileStorage) {
        return images.stream()
                .sorted(Comparator.comparingInt(PlaceImage::getSequence))
                .map(image -> PlaceImageItem.of(image, fileStorage))
                .toList();
    }
}
