package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.Comparator;
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
    private double totalRating;

    @Column(nullable = false)
    private int totalOutletScore;

    @Column(nullable = false)
    private int totalCrowdScore;

    @Column(nullable = false)
    private int totalSpaceSizeScore;

    @Column(nullable = false)
    private int totalQuietScore;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private int wishCount;

    public static PlaceDetail of(
            Place place,
            int outletScore,
            int crowdScore,
            int spaceSizeScore,
            int quietScore
    ) {
        return new PlaceDetail(place, outletScore, crowdScore, spaceSizeScore, quietScore);
    }

    private PlaceDetail(
            Place place,
            int outletScore,
            int crowdScore,
            int spaceSizeScore,
            int quietScore
    ) {
        validatePlace(place);
        validateScore(outletScore, "콘센트 점수");
        validateScore(crowdScore, "혼잡도 점수");
        validateScore(spaceSizeScore, "공간 크기 점수");
        validateScore(quietScore, "분위기 점수");

        this.place = place;
        this.totalRating = 0.0;
        this.totalOutletScore = outletScore;
        this.totalCrowdScore = crowdScore;
        this.totalSpaceSizeScore = spaceSizeScore;
        this.totalQuietScore = quietScore;
        this.reviewCount = 0;
        this.wishCount = 0;
    }

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 정보는 필수입니다.");
        }
    }

    private static void validateScore(int score, String fieldName) {
        if (score < 0 || score > 100) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "는 0~100 사이여야 합니다.");
        }
    }

    public Mood getMood() {
        double averageScore = calculateAverageScore(this.totalQuietScore);
        return Arrays.stream(Mood.values())
                .min(Comparator.comparingDouble(m -> Math.abs(m.getScore() - averageScore)))
                .orElse(Mood.CHATTING);
    }

    public SpaceSize getSpaceSize() {
        double averageScore = calculateAverageScore(this.totalSpaceSizeScore);
        return Arrays.stream(SpaceSize.values())
                .min(Comparator.comparingDouble(s -> Math.abs(s.getScore() - averageScore)))
                .orElse(SpaceSize.MEDIUM);
    }

    public OutletScore getOutletScore() {
        double averageScore = calculateAverageScore(this.totalOutletScore);
        return Arrays.stream(OutletScore.values())
                .min(Comparator.comparingDouble(o -> Math.abs(o.getScore() - averageScore)))
                .orElse(OutletScore.AVERAGE);
    }

    public CrowdStatus getCrowdStatus() {
        double averageScore = calculateAverageScore(this.totalCrowdScore);
        return Arrays.stream(CrowdStatus.values())
                .min(Comparator.comparingDouble(c -> Math.abs(c.getScore() - averageScore)))
                .orElse(CrowdStatus.NORMAL);
    }

    public double getAverageRating() {
        int count = this.reviewCount + 1;
        return this.totalRating / count;
    }

    private double calculateAverageScore(int totalScore) {
        int count = this.reviewCount + 1;
        return (double) totalScore / count;
    }

    public void addReviewScores(ReviewScores scores) {
        this.totalRating += scores.rating();
        this.totalOutletScore += scores.outletScore();
        this.totalCrowdScore += scores.crowdScore();
        this.totalSpaceSizeScore += scores.spaceSizeScore();
        this.totalQuietScore += scores.quietScore();
        this.reviewCount++;
    }

    public void removeReviewScores(ReviewScores scores) {
        if (this.reviewCount <= 0) {
            return;
        }
        this.totalRating -= scores.rating();
        this.totalOutletScore -= scores.outletScore();
        this.totalCrowdScore -= scores.crowdScore();
        this.totalSpaceSizeScore -= scores.spaceSizeScore();
        this.totalQuietScore -= scores.quietScore();
        this.reviewCount--;
    }

    public void updateScore(Mood newMood, SpaceSize newSpaceSize, OutletScore newOutletScore, CrowdStatus newCrowdStatus) {
        if (newMood != null) {
            int oldScore = getMood().getScore();
            this.totalQuietScore += newMood.getScore() - oldScore;
        }
        if (newSpaceSize != null) {
            int oldScore = getSpaceSize().getScore();
            this.totalSpaceSizeScore += newSpaceSize.getScore() - oldScore;
        }
        if (newOutletScore != null) {
            int oldScore = getOutletScore().getScore();
            this.totalOutletScore += newOutletScore.getScore() - oldScore;
        }
        if (newCrowdStatus != null) {
            int oldScore = getCrowdStatus().getScore();
            this.totalCrowdScore += newCrowdStatus.getScore() - oldScore;
        }
    }
}
