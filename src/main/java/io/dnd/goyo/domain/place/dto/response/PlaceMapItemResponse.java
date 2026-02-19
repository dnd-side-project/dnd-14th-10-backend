package io.dnd.goyo.domain.place.dto.response;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.List;

@Schema(description = "지도 공간 아이템")
public record PlaceMapItemResponse(
        @Schema(description = "공간 ID", example = "1")
        Long id,

        @Schema(description = "공간 이름", example = "고작 아지트")
        String name,

        @Schema(description = "카테고리", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "상세 주소", example = "청계천로 101")
        String addressDetail,

        @Schema(description = "행정구역 코드 (10자리)", example = "1168010100")
        Long regionCode,

        @Schema(description = "이미지 목록 (순서 기준 정렬)")
        List<ImageItem> images,

        @Schema(description = "위도", example = "37.5665")
        double latitude,

        @Schema(description = "경도", example = "126.9780")
        double longitude,

        @Schema(description = "분위기", example = "CALM")
        Mood mood,

        @Schema(description = "공간 크기", example = "MEDIUM")
        SpaceSize spaceSize,

        @Schema(description = "찜 수", example = "42")
        int wishCount,

        @Schema(description = "내가 찜했는지 여부", example = "true")
        boolean isWished
) {
    public record ImageItem(
            @Schema(description = "이미지 URL")
            String url,

            @Schema(description = "순서", example = "0")
            int sequence,

            @Schema(description = "대표 이미지 여부", example = "true")
            boolean representativeFlag
    ) {
        public static ImageItem of(PlaceImage image, FileStorage fileStorage) {
            return new ImageItem(
                    fileStorage.generatePublicUrl(image.getImageKey()),
                    image.getSequence(),
                    image.isRepresentativeFlag()
            );
        }
    }

    public static PlaceMapItemResponse of(Place place, PlaceDetail placeDetail, boolean isWished, FileStorage fileStorage) {
        List<ImageItem> images = place.getImages().stream()
                .sorted(Comparator.comparingInt(PlaceImage::getSequence))
                .map(image -> ImageItem.of(image, fileStorage))
                .toList();

        return new PlaceMapItemResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getAddressDetail(),
                place.getRegionCode().value(),
                images,
                place.getLocation().getY(),
                place.getLocation().getX(),
                placeDetail.getMood(),
                placeDetail.getSpaceSize(),
                placeDetail.getWishCount(),
                isWished
        );
    }
}
