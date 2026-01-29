package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record PlaceName(
    String value
) {
    private static final int MAX_LENGTH = 50;

    public PlaceName {
        validateIsNotBlank(value);
        validateLengthIsNotOverMaxLength(value);
    }

    private static void validateIsNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "장소 이름은 필수입니다.");
        }
    }

    private static void validateLengthIsNotOverMaxLength(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, String.format("장소 이름은 %d자 이내여야 합니다.", MAX_LENGTH));
        }
    }
}
