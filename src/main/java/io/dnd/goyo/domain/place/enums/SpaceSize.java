package io.dnd.goyo.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpaceSize {
    SMALL("소형", 0),
    MEDIUM("중형", 50),
    LARGE("대형", 100);

    private final String description;
    private final int score;
}
