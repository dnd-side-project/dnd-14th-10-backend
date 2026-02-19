package io.dnd.goyo.domain.user.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.enums.WithdrawReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_withdraws")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdraw extends BaseEntity {

    private static final int DETAIL_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawReason reason;

    @Column(length = 1000)
    private String detail;

    private UserWithdraw(User user, WithdrawReason reason, String detail) {
        validate(reason, detail);
        this.user = user;
        this.reason = reason;
        this.detail = detail;
    }

    public static UserWithdraw of(User user, WithdrawReason reason, String detail) {
        return new UserWithdraw(user, reason, detail);
    }

    private static void validate(WithdrawReason reason, String detail) {
        if (reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "탈퇴 사유는 필수입니다.");
        }
        if (reason == WithdrawReason.OTHER && (detail == null || detail.isBlank())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "기타 사유를 선택한 경우 상세 내용은 필수입니다.");
        }
        if (detail != null && detail.length() > DETAIL_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("상세 내용은 %d자 이내여야 합니다.", DETAIL_MAX_LENGTH));
        }
    }
}
