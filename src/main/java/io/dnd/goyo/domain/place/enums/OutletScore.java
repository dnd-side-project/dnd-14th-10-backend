package io.dnd.goyo.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutletScore {
    FEW("부족함", 0),
    AVERAGE("적당함", 50),
    MANY("넉넉함", 100);

    private final String description;
    private final int score;
}
