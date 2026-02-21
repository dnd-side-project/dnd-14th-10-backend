package io.dnd.goyo.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawReason {
    LOW_USAGE("서비스를 자주 이용하지 않아요"),
    PRIVACY_CONCERN("개인정보 보호가 걱정돼요"),
    OTHER("기타(직접 입력)");

    private final String description;
}
