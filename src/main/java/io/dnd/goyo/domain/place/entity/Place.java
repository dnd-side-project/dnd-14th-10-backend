package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "places")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", length = 50, nullable = false))
    private PlaceName name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCategory category;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    private Integer floorInfo;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceStatus status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "region_code", nullable = false))
    private RegionCode regionCode;

    @Column(length = 50, nullable = false)
    private String addressDetail;

    // TODO: [User 엔티티 머지 후] User로 변경
    @Column(nullable = false)
    private Long userId;

    // TODO: [Tag 엔티티 머지 후] 태그 추가

    @Builder
    private Place(
            String name,
            PlaceCategory category,
            Point location,
            Integer regionCode,
            String addressDetail,
            Long userId,
            Integer floorInfo,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        validateCategory(category);
        validateLocation(location);
        validateAddressDetail(addressDetail);
        validateUserId(userId);
        validateOperatingHours(openTime, closeTime);

        this.name = new PlaceName(name);
        this.category = category;
        this.location = location;
        this.regionCode = new RegionCode(regionCode);
        this.addressDetail = addressDetail;
        this.userId = userId;
        this.floorInfo = floorInfo;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.status = PlaceStatus.ACTIVE;
    }

    private static final int ADDRESS_DETAIL_MAX_LENGTH = 50;
    private static final double MIN_LATITUDE = 33.0;
    private static final double MAX_LATITUDE = 43.0;
    private static final double MIN_LONGITUDE = 124.0;
    private static final double MAX_LONGITUDE = 132.0;

    private void validateCategory(PlaceCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카테고리는 필수입니다.");
        }
    }

    private void validateLocation(Point location) {
        if (location == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "위치 정보는 필수입니다.");
        }

        double lat = location.getY();
        double lng = location.getX();

        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE || lng < MIN_LONGITUDE || lng > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효한 대한민국 좌표가 아닙니다.");
        }
    }

    private void validateAddressDetail(String addressDetail) {
        if (addressDetail == null || addressDetail.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상세 주소는 필수입니다.");
        }
        if (addressDetail.length() > ADDRESS_DETAIL_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, String.format("상세 주소는 %d자 이내여야 합니다.", ADDRESS_DETAIL_MAX_LENGTH));
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "등록자 ID는 필수입니다.");
        }
    }

    private void validateOperatingHours(LocalTime openTime, LocalTime closeTime) {
        boolean hasOpenTime = (openTime != null);
        boolean hasCloseTime = (closeTime != null);

        if (hasOpenTime != hasCloseTime) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "영업시간은 시작과 종료를 함께 입력해야 합니다.");
        }
    }
}
