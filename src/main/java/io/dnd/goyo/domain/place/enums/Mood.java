package io.dnd.goyo.domain.place.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mood {
    NOISY("소란스러움", 25),
    CHATTING("대화하는 분위기", 50),
    CALM("차분한 분위기", 75),
    SILENT("고요해요", 100);

    private final String description;
    private final int score;
}
