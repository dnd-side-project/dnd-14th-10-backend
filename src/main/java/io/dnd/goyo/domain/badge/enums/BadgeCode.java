package io.dnd.goyo.domain.badge.enums;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeCode {

    REVIEW_1("REVIEW_1", ActivityType.REVIEW, 1, "첫 리뷰"),
    REVIEW_25("REVIEW_25", ActivityType.REVIEW, 25, "리뷰 25개 작성"),
    REVIEW_80("REVIEW_80", ActivityType.REVIEW, 80, "리뷰 80개 작성"),

    PLACE_1("PLACE_1", ActivityType.PLACE, 1, "첫 장소 등록"),
    PLACE_7("PLACE_7", ActivityType.PLACE, 7, "장소 7개 등록"),
    PLACE_20("PLACE_20", ActivityType.PLACE, 20, "장소 20개 등록"),

    IMAGE_5("IMAGE_5", ActivityType.IMAGE, 5, "이미지 포함 활동 5회"),
    IMAGE_30("IMAGE_30", ActivityType.IMAGE, 30, "이미지 포함 활동 30회"),
    IMAGE_80("IMAGE_80", ActivityType.IMAGE, 80, "이미지 포함 활동 80회");

    private final String code;
    private final ActivityType activityType;
    private final int threshold;
    private final String displayName;

    private static final Map<ActivityType, List<BadgeCode>> CACHE =
            Arrays.stream(values())
                    .collect(Collectors.groupingBy(
                            BadgeCode::getActivityType,
                            () -> new EnumMap<>(ActivityType.class),
                            Collectors.toUnmodifiableList()));

    public static List<BadgeCode> getByActivityType(ActivityType activityType) {
        return CACHE.getOrDefault(activityType, List.of());
    }
}
