package io.dnd.goyo.domain.user.dto.request;

import io.dnd.goyo.domain.user.enums.WithdrawReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawRequest(
        @NotNull(message = "탈퇴 사유는 필수입니다")
        WithdrawReason reason,

        @Size(max = 1000, message = "상세 내용은 1000자 이내여야 합니다")
        String detail
) {
}
