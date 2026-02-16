package io.dnd.goyo.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    TEENAGER(10, 19),
    TWENTIES(20, 29),
    THIRTIES(30, 39),
    FORTIES(40, 49),
    FIFTIES(50, 59),
    SIXTIES_AND_ABOVE(60, 999);

    private final int minAge;
    private final int maxAge;

    public static AgeGroup from(Integer age) {
        if (age == null) {
            return null;
        }
        for (AgeGroup group : values()) {
            if (age >= group.minAge && age <= group.maxAge) {
                return group;
            }
        }
        throw new IllegalArgumentException("Invalid age: " + age);
    }
}
