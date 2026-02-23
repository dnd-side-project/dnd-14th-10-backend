package io.dnd.goyo.domain.wishlist.dto.response;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.Mood;
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
        @Schema(description = "대표 이미지 URL")
        String representativeImageUrl,
        @Schema(description = "분위기", example = "CALM")
        Mood mood,
        @Schema(description = "공간 크기", example = "MEDIUM")
        SpaceSize spaceSize,
        @Schema(description = "찜 수", example = "42")
        int wishCount,
        @Schema(description = "찜 여부", example = "true")
        boolean wished,
        @Schema(description = "찜한 시각")
        LocalDateTime wishedAt
) {

    public static WishlistItemResponse from(Wishlist wishlist, FileStorage fileStorage) {
        Place place = wishlist.getPlace();
        PlaceDetail placeDetail = place.getPlaceDetail();

        return new WishlistItemResponse(
                wishlist.getId(),
                place.getId(),
                place.getName(),
                fileStorage.generatePublicUrl(place.getRepresentativeImageKey()),
                placeDetail.getMood(),
                placeDetail.getSpaceSize(),
                placeDetail.getWishCount(),
                true,
                wishlist.getCreatedAt()
        );
    }
}
