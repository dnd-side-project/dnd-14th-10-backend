package io.dnd.goyo.domain.place.enums;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RandomThemeType {
    MOOD(Mood.SILENT.name(), "total_quiet_score", topRangeMin(Mood.values().length), 100.0),
    SPACE_SIZE(SpaceSize.LARGE.name(), "total_space_size_score", topRangeMin(SpaceSize.values().length), 100.0),
    OUTLET(OutletScore.MANY.name(), "total_outlet_score", topRangeMin(OutletScore.values().length), 100.0),
    CROWD(CrowdStatus.RELAX.name(), "total_crowd_score", 0.0, bottomRangeMax(CrowdStatus.values().length));

    private final String themeValue;
    private final String scoreColumn;
    private final double minScore;
    private final double maxScore;

    public static RandomThemeType pick() {
        RandomThemeType[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private static double topRangeMin(int rangeCount) {
        return 100.0 * (rangeCount - 1) / rangeCount;
    }

    private static double bottomRangeMax(int rangeCount) {
        return 100.0 / rangeCount;
    }
}
