package io.dnd.goyo.domain.user.enums;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import java.util.Arrays;

public enum Provider {
    KAKAO,
    NAVER;

    public static Provider from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "OAuth 제공자는 필수입니다.");
        }

        return Arrays.stream(values())
                .filter(provider -> provider.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT,
                        "지원하지 않는 OAuth 제공자입니다: " + value));
    }
}
