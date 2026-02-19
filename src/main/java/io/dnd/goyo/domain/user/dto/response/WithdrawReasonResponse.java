package io.dnd.goyo.domain.user.dto.response;

import io.dnd.goyo.domain.user.enums.WithdrawReason;

public record WithdrawReasonResponse(
        String code,
        String description
) {
    public static WithdrawReasonResponse from(WithdrawReason reason) {
        return new WithdrawReasonResponse(reason.name(), reason.getDescription());
    }
}
