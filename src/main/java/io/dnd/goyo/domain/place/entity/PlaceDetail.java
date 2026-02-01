package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceDetail extends BaseEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false)
    private double rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutletScore outletScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrowdStatus crowdStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceSize spaceSize;

    @Column(nullable = false)
    private int wishCount;

    public static PlaceDetail of(
            Place place,
            double rating,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize
    ) {
        return new PlaceDetail(place, rating, outletScore, crowdStatus, spaceSize);
    }

    private PlaceDetail(
            Place place,
            double rating,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize
    ) {
        validatePlace(place);
        validateRating(rating);
        validateOutletScore(outletScore);
        validateCrowdStatus(crowdStatus);
        validateSpaceSize(spaceSize);

        this.place = place;
        this.rating = rating;
        this.outletScore = outletScore;
        this.crowdStatus = crowdStatus;
        this.spaceSize = spaceSize;
        this.wishCount = 0;
    }

    private static final double MIN_RATING = 0.0;
    private static final double MAX_RATING = 5.0;

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 정보는 필수입니다.");
        }
    }

    private static void validateRating(double rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, String.format("평점은 %.1f ~ %.1f 사이여야 합니다.", MIN_RATING, MAX_RATING));
        }
    }

    private static void validateOutletScore(OutletScore outletScore) {
        if (outletScore == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "콘센트 점수는 필수입니다.");
        }
    }

    private static void validateCrowdStatus(CrowdStatus crowdStatus) {
        if (crowdStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "혼잡도는 필수입니다.");
        }
    }

    private static void validateSpaceSize(SpaceSize spaceSize) {
        if (spaceSize == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "공간 크기는 필수입니다.");
        }
    }
}
