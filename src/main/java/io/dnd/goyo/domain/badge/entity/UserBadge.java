package io.dnd.goyo.domain.badge.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_badges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    public static UserBadge of(User user, Badge badge) {
        return new UserBadge(user, badge);
    }

    private UserBadge(User user, Badge badge) {
        validateUser(user);
        validateBadge(badge);

        this.user = user;
        this.badge = badge;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 정보는 필수입니다.");
        }
    }

    private static void validateBadge(Badge badge) {
        if (badge == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "뱃지 정보는 필수입니다.");
        }
    }
}
