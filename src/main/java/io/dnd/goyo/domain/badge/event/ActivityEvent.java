package io.dnd.goyo.domain.badge.event;

import io.dnd.goyo.domain.badge.enums.ActivityType;

public record ActivityEvent(
        Long userId,
        ActivityType activityType,
        int countDelta,
        int imageDelta
) {
}
