package io.dnd.goyo.domain.place.dto.response;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.List;
import org.locationtech.jts.geom.Point;

public record PlaceDetailResponse(
        @Schema(description = "공간 ID", example = "1")
        Long id,

        @Schema(description = "공간 이름", example = "고작 아지트 강남점")
        String name,

        @Schema(description = "카테고리", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "상세 주소", example = "서울 강남구 강남대로 123")
        String addressDetail,

        @Schema(description = "위도", example = "37.123456")
        double latitude,

        @Schema(description = "경도", example = "127.123456")
        double longitude,

        @Schema(description = "이미지 목록 (순서 기준 정렬)")
        List<PlaceImageItem> images,

        @Schema(description = "평균 별점", example = "4.5")
        double averageRating,

        @Schema(description = "리뷰 수", example = "10")
        int reviewCount,

        @Schema(description = "공간 크기", example = "LARGE")
        SpaceSize spaceSize,

        @Schema(description = "공간 분위기", example = "CALM")
        Mood mood,

        @Schema(description = "콘센트", example = "MANY")
        OutletScore outletScore,

        @Schema(description = "혼잡도", example = "RELAX")
        CrowdStatus crowdStatus,

        @Schema(description = "오픈 시간", example = "09:00")
        LocalTime openTime,

        @Schema(description = "마감 시간", example = "22:00")
        LocalTime closeTime,

        @Schema(description = "층 정보", example = "1")
        Integer floorInfo,

        @Schema(description = "화장실 정보", example = "내부")
        String restroomInfo,

        @Schema(description = "위시 여부", example = "true")
        boolean isWished
) {
    public static PlaceDetailResponse from(Place place, boolean isWished, FileStorage fileStorage) {
        PlaceDetail detail = place.getPlaceDetail();
        Point location = place.getLocation();
        List<PlaceImageItem> images = PlaceImageItem.listOf(place.getImages(), fileStorage);

        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getAddressDetail(),
                location.getY(),
                location.getX(),
                images,
                detail.getAverageRating(),
                detail.getReviewCount(),
                detail.getSpaceSize(),
                detail.getMood(),
                detail.getOutletScore(),
                detail.getCrowdStatus(),
                place.getOpenTime(),
                place.getCloseTime(),
                place.getFloorInfo(),
                place.getRestroomInfo(),
                isWished
        );
    }
}
