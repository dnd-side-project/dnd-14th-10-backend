package io.dnd.goyo.domain.place.enums;

import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RandomThemeType {
    MOOD(Mood.SILENT.name()),
    SPACE_SIZE(SpaceSize.LARGE.name()),
    OUTLET(OutletScore.MANY.name()),
    CROWD(CrowdStatus.RELAX.name());

    private final String themeValue;

    public static RandomThemeType pick(Random random) {
        RandomThemeType[] values = values();
        return values[random.nextInt(values.length)];
    }
}