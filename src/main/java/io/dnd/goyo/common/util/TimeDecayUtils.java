package io.dnd.goyo.common.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeDecayUtils {

    private static final double SECONDS_PER_DAY = 86400.0;
    private static final double DECAY_CONSTANT = 30.0;

    public static double calculateWeight(LocalDateTime createdAt, LocalDateTime now) {
        double daysAgo = ChronoUnit.SECONDS.between(createdAt, now) / SECONDS_PER_DAY;
        return Math.exp(-daysAgo / DECAY_CONSTANT);
    }
}
