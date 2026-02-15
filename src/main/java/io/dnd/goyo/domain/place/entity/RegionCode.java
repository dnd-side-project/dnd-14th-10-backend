package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public record RegionCode(
        Long value
) {
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 10;

    public RegionCode {
        validateIsNotNull(value);
        validateCodeLength(value);
    }

    public int getSiGunGuCode() {
        if (value > 99999) {
            return (int) (value / 100000);
        }
        return value.intValue();
    }

    private static void validateIsNotNull(Long value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "행정구역 코드는 필수입니다.");
        }
    }

    private static void validateCodeLength(Long value) {
        int length = String.valueOf(value).length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    String.format("행정구역 코드는 %d~%d자리여야 합니다.", MIN_LENGTH, MAX_LENGTH)
            );
        }
    }
}
