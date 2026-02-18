package io.dnd.goyo.domain.wishlist.dto.response;

import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "찜 목록 아이템 응답")
public record WishlistItemResponse(
        @Schema(description = "찜 ID", example = "1")
        Long wishlistId,
        @Schema(description = "공간 ID", example = "10")
        Long placeId,
        @Schema(description = "공간 이름", example = "고작 아지트")
        String placeName,
        @Schema(description = "카테고리", example = "CAFE")
        PlaceCategory category,
        @Schema(description = "상세 주소", example = "청계천로 101")
        String addressDetail,
        @Schema(description = "행정구역 코드", example = "1168010100")
        Long regionCode,
        @Schema(description = "대표 이미지 키")
        String representativeImageKey,
        @Schema(description = "위도", example = "37.5665")
        double latitude,
        @Schema(description = "경도", example = "126.9780")
        double longitude,
        @Schema(description = "분위기", example = "CALM")
        Mood mood,
        @Schema(description = "공간 크기", example = "MEDIUM")
        SpaceSize spaceSize,
        @Schema(description = "찜한 시각")
        LocalDateTime wishedAt
) {

    public static WishlistItemResponse from(Wishlist wishlist) {
        Place place = wishlist.getPlace();
        PlaceDetail placeDetail = place.getPlaceDetail();

        return new WishlistItemResponse(
                wishlist.getId(),
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getAddressDetail(),
                place.getRegionCode().value(),
                place.getRepresentativeImageKey(),
                place.getLocation().getY(),
                place.getLocation().getX(),
                placeDetail != null ? placeDetail.getMood() : null,
                placeDetail != null ? placeDetail.getSpaceSize() : null,
                wishlist.getCreatedAt()
        );
    }
}
