package io.dnd.goyo.domain.user.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStats extends BaseEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private int placeCount;

    @Column(nullable = false)
    private int badgeCount;

    @Column(nullable = false)
    private int imageCount;

    public static UserStats of(User user) {
        return new UserStats(user);
    }

    private UserStats(User user) {
        validateUser(user);

        this.user = user;
        this.reviewCount = 0;
        this.placeCount = 0;
        this.badgeCount = 0;
        this.imageCount = 0;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 정보는 필수입니다.");
        }
    }

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public void incrementPlaceCount() {
        this.placeCount++;
    }

    public void incrementBadgeCount() {
        this.badgeCount++;
    }

    public void incrementImageCount() {
        this.imageCount++;
    }

    public void decrementReviewCount() {
        this.reviewCount--;
    }

    public void decrementPlaceCount() {
        this.placeCount--;
    }

    public void decrementImageCount() {
        this.imageCount--;
    }
}
