package io.dnd.goyo.common.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {
    PLACE("place"),
    REVIEW("review"),
    USER("user");

    private final String path;
}
