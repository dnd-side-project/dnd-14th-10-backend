package io.dnd.goyo.domain.badge.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse;
import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse.BadgeProgress;
import io.dnd.goyo.domain.badge.dto.response.BadgeProgressResponse.CategoryProgress;
import io.dnd.goyo.domain.badge.entity.UserBadge;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.enums.BadgeCode;
import io.dnd.goyo.domain.badge.repository.UserBadgeRepository;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final UserStatsRepository userStatsRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeProgressResponse getBadgeProgress(Long userId) {
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserBadge> userBadges = userBadgeRepository.findAllByUserId(userId);
        Map<String, UserBadge> achievedBadgeMap = userBadges.stream()
                .collect(Collectors.toMap(
                        ub -> ub.getBadge().getCode(),
                        ub -> ub
                ));

        List<CategoryProgress> categories = Arrays.stream(ActivityType.values())
                .map(activityType -> buildCategoryProgress(activityType, userStats, achievedBadgeMap))
                .toList();

        return new BadgeProgressResponse(categories);
    }

    private CategoryProgress buildCategoryProgress(
            ActivityType activityType,
            UserStats userStats,
            Map<String, UserBadge> achievedBadgeMap
    ) {
        int currentCount = getCurrentCount(userStats, activityType);

        List<BadgeProgress> badges = BadgeCode.getByActivityType(activityType).stream()
                .map(badgeCode -> {
                    UserBadge userBadge = achievedBadgeMap.get(badgeCode.getCode());
                    return new BadgeProgress(
                            badgeCode.getCode(),
                            badgeCode.getDisplayName(),
                            badgeCode.getThreshold(),
                            userBadge != null,
                            userBadge != null ? userBadge.getCreatedAt() : null
                    );
                })
                .toList();

        return new CategoryProgress(activityType, currentCount, badges);
    }

    private int getCurrentCount(UserStats userStats, ActivityType activityType) {
        return switch (activityType) {
            case REVIEW -> userStats.getReviewCount();
            case PLACE -> userStats.getPlaceCount();
            case IMAGE -> userStats.getImageCount();
        };
    }
}
