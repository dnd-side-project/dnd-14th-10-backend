package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record RegionCode(
    Integer value
) {
    private static final int CODE_LENGTH = 5;
    private static final int MIN_VALUE = 10000;
    private static final int MAX_VALUE = 99999;

    public RegionCode {
        validateIsNotNull(value);
        validateCodeRange(value);
    }

    private static void validateIsNotNull(Integer value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "행정구역 코드는 필수입니다.");
        }
    }

    private static void validateCodeRange(Integer value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, String.format("행정구역 코드는 %d자리여야  합니다.", CODE_LENGTH));
        }
    }
}
