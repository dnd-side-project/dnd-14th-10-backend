package io.dnd.goyo.domain.place.dto.response;

import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공간 요약 정보 응답")
public record PlaceSummaryResponse(
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

        @Schema(description = "대표 이미지 URL", example = "https://...")
        String representativeImageUrl,

        @Schema(description = "위도", example = "37.5665")
        double latitude,

        @Schema(description = "경도", example = "126.9780")
        double longitude,

        @Schema(description = "분위기", example = "CALM")
        Mood mood,

        @Schema(description = "공간 크기", example = "MEDIUM")
        SpaceSize spaceSize,

        @Schema(description = "내가 찜했는지 여부", example = "true")
        boolean isWished
) {
    public static PlaceSummaryResponse of(
            Place place,
            PlaceDetail placeDetail,
            String representativeImageUrl,
            boolean isWished
    ) {
        return new PlaceSummaryResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getAddressDetail(),
                place.getRegionCode().value(),
                representativeImageUrl,
                place.getLocation().getY(),
                place.getLocation().getX(),
                placeDetail.getMood(),
                placeDetail.getSpaceSize(),
                isWished
        );
    }
}
