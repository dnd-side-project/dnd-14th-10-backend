package io.dnd.goyo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.WithdrawReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserWithdrawTest {

    private static User createValidUser() {
        return User.builder()
                .name("김고작")
                .nickname("고작이")
                .gender(Gender.MALE)
                .provider(Provider.KAKAO)
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("정상적으로 UserWithdraw를 생성할 수 있다")
    void 정상_생성() {
        // given
        User user = createValidUser();

        // when
        UserWithdraw withdraw = UserWithdraw.of(user, WithdrawReason.LOW_USAGE, null);

        // then
        assertThat(withdraw.getUser()).isEqualTo(user);
        assertThat(withdraw.getReason()).isEqualTo(WithdrawReason.LOW_USAGE);
        assertThat(withdraw.getDetail()).isNull();
    }

    @Test
    @DisplayName("OTHER 사유에 상세 내용을 함께 생성할 수 있다")
    void OTHER_사유_상세내용_포함() {
        // given
        User user = createValidUser();

        // when
        UserWithdraw withdraw = UserWithdraw.of(user, WithdrawReason.OTHER, "다른 앱을 사용하려고요");

        // then
        assertThat(withdraw.getReason()).isEqualTo(WithdrawReason.OTHER);
        assertThat(withdraw.getDetail()).isEqualTo("다른 앱을 사용하려고요");
    }

    @Test
    @DisplayName("reason이 null이면 예외가 발생한다")
    void reason_null이면_예외() {
        // given
        User user = createValidUser();

        // when & then
        assertThatThrownBy(() -> UserWithdraw.of(user, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("OTHER 사유인데 detail이 없으면 예외가 발생한다")
    void OTHER_사유_detail_없으면_예외() {
        // given
        User user = createValidUser();

        // when & then
        assertThatThrownBy(() -> UserWithdraw.of(user, WithdrawReason.OTHER, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("OTHER 사유인데 detail이 빈 문자열이면 예외가 발생한다")
    void OTHER_사유_detail_빈문자열이면_예외() {
        // given
        User user = createValidUser();

        // when & then
        assertThatThrownBy(() -> UserWithdraw.of(user, WithdrawReason.OTHER, "   "))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    void detail_1000자_초과_예외() {
        // given
        User user = createValidUser();
        String longDetail = "a".repeat(1001);

        // when & then
        assertThatThrownBy(() -> UserWithdraw.of(user, WithdrawReason.OTHER, longDetail))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
    }

    @Test
    void detail_1000자_정상() {
        // given
        User user = createValidUser();
        String detail = "a".repeat(1000);

        // when
        UserWithdraw withdraw = UserWithdraw.of(user, WithdrawReason.OTHER, detail);

        // then
        assertThat(withdraw.getDetail()).hasSize(500);
    }
}
