package io.dnd.goyo.domain.badge.enums;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeCode {

    REVIEW_1("REVIEW_1", ActivityType.REVIEW, 1),
    REVIEW_25("REVIEW_25", ActivityType.REVIEW, 25),
    REVIEW_80("REVIEW_80", ActivityType.REVIEW, 80),

    PLACE_1("PLACE_1", ActivityType.PLACE, 1),
    PLACE_7("PLACE_7", ActivityType.PLACE, 7),
    PLACE_20("PLACE_20", ActivityType.PLACE, 20),

    IMAGE_5("IMAGE_5", ActivityType.IMAGE, 5),
    IMAGE_30("IMAGE_30", ActivityType.IMAGE, 30),
    IMAGE_80("IMAGE_80", ActivityType.IMAGE, 80);

    private final String code;
    private final ActivityType activityType;
    private final int threshold;

    public static List<BadgeCode> getByActivityType(ActivityType activityType) {
        return Arrays.stream(values())
                .filter(badge -> badge.activityType == activityType)
                .toList();
    }
}
