package io.dnd.goyo.domain.badge.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserBadgeTest {

    @Nested
    @DisplayName("UserBadge 생성 시")
    class CreateUserBadge {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // given
            Badge badge = mock(Badge.class);

            // when & then
            assertThatThrownBy(() -> UserBadge.of(null, badge))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 뱃지_정보가_null이면_예외_발생() {
            // given
            User user = mock(User.class);

            // when & then
            assertThatThrownBy(() -> UserBadge.of(user, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("뱃지 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);
            Badge badge = mock(Badge.class);

            // when
            UserBadge userBadge = UserBadge.of(user, badge);

            // then
            assertThat(userBadge).isNotNull();
            assertThat(userBadge.getUser()).isEqualTo(user);
            assertThat(userBadge.getBadge()).isEqualTo(badge);
        }
    }
}
