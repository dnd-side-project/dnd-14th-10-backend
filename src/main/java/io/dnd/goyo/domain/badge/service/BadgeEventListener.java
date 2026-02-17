package io.dnd.goyo.domain.badge.service;

import io.dnd.goyo.domain.badge.entity.Badge;
import io.dnd.goyo.domain.badge.entity.UserBadge;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.enums.BadgeCode;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.badge.repository.BadgeRepository;
import io.dnd.goyo.domain.badge.repository.UserBadgeRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import io.dnd.goyo.domain.user.service.UserReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BadgeEventListener {

    private final UserStatsRepository userStatsRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserReader userReader;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleActivity(ActivityEvent event) {
        UserStats userStats = userStatsRepository.findByUserId(event.userId())
                .orElse(null);

        if (userStats == null) {
            log.warn("UserStats not found for userId: {}", event.userId());
            return;
        }

        updateCounts(userStats, event);
        evaluateBadges(event.userId(), userStats, event);
    }

    private void updateCounts(UserStats userStats, ActivityEvent event) {
        if (event.activityType() == ActivityType.REVIEW) {
            if (event.countDelta() > 0) {
                userStats.incrementReviewCount();
            } else if (event.countDelta() < 0) {
                userStats.decrementReviewCount();
            }
        } else if (event.activityType() == ActivityType.PLACE) {
            if (event.countDelta() > 0) {
                userStats.incrementPlaceCount();
            } else if (event.countDelta() < 0) {
                userStats.decrementPlaceCount();
            }
        }

        if (event.imageDelta() > 0) {
            userStats.incrementImageCount();
        } else if (event.imageDelta() < 0) {
            userStats.decrementImageCount();
        }
    }

    private void evaluateBadges(Long userId, UserStats userStats, ActivityEvent event) {
        if (event.countDelta() > 0) {
            int currentCount = getCurrentCount(userStats, event.activityType());
            List<BadgeCode> badgeCodes = BadgeCode.getByActivityType(event.activityType());
            awardBadgesIfEligible(userId, badgeCodes, currentCount, userStats);
        }

        if (event.imageDelta() > 0) {
            int imageCount = userStats.getImageCount();
            List<BadgeCode> imageBadgeCodes = BadgeCode.getByActivityType(ActivityType.IMAGE);
            awardBadgesIfEligible(userId, imageBadgeCodes, imageCount, userStats);
        }
    }

    private int getCurrentCount(UserStats userStats, ActivityType activityType) {
        return switch (activityType) {
            case REVIEW -> userStats.getReviewCount();
            case PLACE -> userStats.getPlaceCount();
            case IMAGE -> userStats.getImageCount();
        };
    }

    private void awardBadgesIfEligible(Long userId, List<BadgeCode> badgeCodes, int currentCount, UserStats userStats) {
        List<String> eligibleCodes = badgeCodes.stream()
                .filter(bc -> currentCount >= bc.getThreshold())
                .map(BadgeCode::getCode)
                .toList();

        if (eligibleCodes.isEmpty()) return;

        User user = userReader.getUser(userId);
        List<Badge> badges = badgeRepository.findByCodeIn(eligibleCodes);
        for (Badge badge : badges) {
            awardBadgeIfNotExists(user, badge, userStats);
        }
    }

    private void awardBadgeIfNotExists(User user, Badge badge, UserStats userStats) {
        if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
            userBadgeRepository.save(UserBadge.of(user, badge));
            userStats.incrementBadgeCount();
            log.info("Badge awarded: userId={}, badgeCode={}", user.getId(), badge.getCode());
        }
    }
}
