package io.dnd.goyo.domain.review.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    private Integer rating;

    @Enumerated(EnumType.STRING)
    private Mood mood;

    @Enumerated(EnumType.STRING)
    private OutletScore outletScore;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private CrowdStatus crowdStatus;

    @Enumerated(EnumType.STRING)
    private SpaceSize spaceSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    private LocalDateTime visitedAt;

    private Review(
            User user,
            Place place,
            Integer rating,
            Mood mood,
            OutletScore outletScore,
            String content,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize,
            LocalDateTime visitedAt
    ) {
        validateUser(user);
        validatePlace(place);
        validateRating(rating);

        this.user = user;
        this.place = place;
        this.rating = rating;
        this.mood = mood;
        this.outletScore = outletScore;
        this.content = content;
        this.crowdStatus = crowdStatus;
        this.spaceSize = spaceSize;
        this.visitedAt = visitedAt;
        this.status = ReviewStatus.ACTIVE;
    }

    public static Review create(
            User user,
            Place place,
            Integer rating,
            Mood mood,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize,
            String content,
            LocalDateTime visitedAt
    ) {
        return new Review(user, place, rating, mood, outletScore, content,
                crowdStatus, spaceSize, visitedAt);
    }

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 정보는 필수입니다.");
        }
    }

    private static void validatePlace(Place place) {
        if (place == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 정보는 필수입니다.");
        }
    }

    private static void validateRating(Integer rating) {
        if (rating != null && (rating < MIN_RATING || rating > MAX_RATING)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("평점은 %d ~ %d 사이여야 합니다.", MIN_RATING, MAX_RATING));
        }
    }

    public void delete() {
        this.status = ReviewStatus.DELETED;
    }

    public void hide() {
        this.status = ReviewStatus.HIDDEN;
    }

    public void update(
            Integer rating,
            Mood mood,
            OutletScore outletScore,
            CrowdStatus crowdStatus,
            SpaceSize spaceSize,
            String content,
            LocalDateTime visitedAt
    ) {
        validateRating(rating);
        this.rating = rating;
        this.mood = mood;
        this.outletScore = outletScore;
        this.crowdStatus = crowdStatus;
        this.spaceSize = spaceSize;
        this.content = content;
        this.visitedAt = visitedAt;
    }
}
