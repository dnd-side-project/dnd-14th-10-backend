package io.dnd.goyo.domain.badge.dto.response;

import io.dnd.goyo.domain.badge.enums.ActivityType;
import java.time.LocalDateTime;
import java.util.List;

public record BadgeProgressResponse(
        List<CategoryProgress> categories
) {

    public record CategoryProgress(
            ActivityType activityType,
            int currentCount,
            List<BadgeProgress> badges
    ) {
    }

    public record BadgeProgress(
            String code,
            String name,
            int threshold,
            boolean achieved,
            LocalDateTime achievedAt
    ) {
    }
}
