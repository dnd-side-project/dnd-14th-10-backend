package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import io.dnd.goyo.domain.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    private static final int NAME_MAX_LENGTH = 50;
    private static final int ADDRESS_DETAIL_MAX_LENGTH = 50;
    private static final double MIN_LATITUDE = 33.0;
    private static final double MAX_LATITUDE = 43.0;
    private static final double MIN_LONGITUDE = 124.0;
    private static final double MAX_LONGITUDE = 132.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCategory category;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    private Integer floorInfo;

    private LocalTime openTime;

    private LocalTime closeTime;

    @Column(length = 10)
    private String restroomInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceStatus status;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "region_code", nullable = false))
    private RegionCode regionCode;

    @Column(length = 50, nullable = false)
    private String addressDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> images = new ArrayList<>();

    @Builder
    private Place(
            String name,
            PlaceCategory category,
            Point location,
            Integer regionCode,
            String addressDetail,
            User user,
            Integer floorInfo,
            LocalTime openTime,
            LocalTime closeTime,
            String restroomInfo
    ) {
        validateName(name);
        validateCategory(category);
        validateLocation(location);
        validateAddressDetail(addressDetail);
        validateUser(user);
        validateOperatingHours(openTime, closeTime);

        this.name = name;
        this.category = category;
        this.location = location;
        this.regionCode = new RegionCode(regionCode);
        this.addressDetail = addressDetail;
        this.user = user;
        this.floorInfo = floorInfo;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.restroomInfo = restroomInfo;
        this.status = PlaceStatus.ACTIVE;
    }

    public void addImage(PlaceImage image) {
        images.add(image);
        image.assignPlace(this);
    }

    public void addImages(List<PlaceImage> newImages) {
        if (newImages == null || newImages.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지는 최소 1장 이상이어야 합니다.");
        }

        List<PlaceImage> allImages = mergeImages(newImages);
        validateRepresentativeImage(allImages);
        validateImageSequence(allImages);

        newImages.forEach(this::addImage);
    }

    private List<PlaceImage> mergeImages(List<PlaceImage> newImages) {
        List<PlaceImage> allImages = new ArrayList<>(this.images);
        allImages.addAll(newImages);
        return allImages;
    }

    private void validateRepresentativeImage(List<PlaceImage> allImages) {
        long representativeCount = allImages.stream()
                .filter(PlaceImage::isRepresentativeFlag)
                .count();
        if (representativeCount != 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "대표 이미지는 정확히 1개여야 합니다.");
        }
    }

    private void validateImageSequence(List<PlaceImage> allImages) {
        long uniqueCount = allImages.stream()
                .map(PlaceImage::getSequence)
                .distinct()
                .count();
        if (uniqueCount != allImages.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미지 순서는 중복될 수 없습니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 이름은 필수입니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("장소 이름은 %d자 이내여야 합니다.", NAME_MAX_LENGTH));
        }
    }

    private static void validateCategory(PlaceCategory category) {
        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "카테고리는 필수입니다.");
        }
    }

    private static void validateLocation(Point location) {
        if (location == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "위치 정보는 필수입니다.");
        }
        double lat = location.getY();
        double lng = location.getX();
        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE || lng < MIN_LONGITUDE || lng > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "유효한 대한민국 좌표가 아닙니다.");
        }
    }

    private static void validateAddressDetail(String addressDetail) {
        if (addressDetail == null || addressDetail.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "상세 주소는 필수입니다.");
        }
        if (addressDetail.length() > ADDRESS_DETAIL_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("상세 주소는 %d자 이내여야 합니다.", ADDRESS_DETAIL_MAX_LENGTH));
        }
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "등록자 정보는 필수입니다.");
        }
    }

    private static void validateOperatingHours(LocalTime openTime, LocalTime closeTime) {
        boolean hasOpenTime = (openTime != null);
        boolean hasCloseTime = (closeTime != null);
        if (hasOpenTime != hasCloseTime) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "영업시간은 시작과 종료를 함께 입력해야 합니다.");
        }
    }
}
