package io.dnd.goyo.domain.place.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Embeddable;

@Embeddable
public class RegionCode {

    private static final int SI_GUN_GU_CODE_LENGTH = 5;
    private static final int LEGAL_DONG_CODE_LENGTH = 10;

    private Long value;

    protected RegionCode() {}

    public RegionCode(Long value) {
        validateIsNotNull(value);
        validateIsPositive(value);
        validateCodeLength(value);
        this.value = value;
    }

    public Long getValue() {
        return value;
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

    private static void validateIsPositive(Long value) {
        if (value <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "행정구역 코드는 양수여야 합니다.");
        }
    }

    private static void validateCodeLength(Long value) {
        int length = String.valueOf(value).length();
        if (length != SI_GUN_GU_CODE_LENGTH && length != LEGAL_DONG_CODE_LENGTH) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    String.format("행정구역 코드는 %d자리(시군구) 또는 %d자리(법정동)여야 합니다.",
                            SI_GUN_GU_CODE_LENGTH, LEGAL_DONG_CODE_LENGTH)
            );
        }
    }
}
