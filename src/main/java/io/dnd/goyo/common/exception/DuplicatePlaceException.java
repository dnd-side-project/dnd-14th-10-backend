package io.dnd.goyo.common.exception;

import lombok.Getter;

@Getter
public class DuplicatePlaceException extends RuntimeException {

    private final Long existingPlaceId;
    private final String existingPlaceName;

    public DuplicatePlaceException(Long existingPlaceId, String existingPlaceName) {
        super(ErrorCode.PLACE_DUPLICATE.getMessage());
        this.existingPlaceId = existingPlaceId;
        this.existingPlaceName = existingPlaceName;
    }
}
