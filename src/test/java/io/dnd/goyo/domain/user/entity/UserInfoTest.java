package io.dnd.goyo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserInfoTest {

    @Nested
    @DisplayName("UserInfo 생성 시")
    class CreateUserInfo {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> UserInfo.of(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성하면_카운트는_모두_0() {
            // given
            User user = mock(User.class);

            // when
            UserInfo userInfo = UserInfo.of(user);

            // then
            assertThat(userInfo.getReviewCount()).isEqualTo(0);
            assertThat(userInfo.getPlaceCount()).isEqualTo(0);
            assertThat(userInfo.getBadgeCount()).isEqualTo(0);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);

            // when
            UserInfo userInfo = UserInfo.of(user);

            // then
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.getUser()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("카운트 증가 시")
    class IncrementCount {

        @Test
        void 리뷰_카운트_증가() {
            // given
            User user = mock(User.class);
            UserInfo userInfo = UserInfo.of(user);

            // when
            userInfo.incrementReviewCount();

            // then
            assertThat(userInfo.getReviewCount()).isEqualTo(1);
        }

        @Test
        void 장소_카운트_증가() {
            // given
            User user = mock(User.class);
            UserInfo userInfo = UserInfo.of(user);

            // when
            userInfo.incrementPlaceCount();

            // then
            assertThat(userInfo.getPlaceCount()).isEqualTo(1);
        }

        @Test
        void 뱃지_카운트_증가() {
            // given
            User user = mock(User.class);
            UserInfo userInfo = UserInfo.of(user);

            // when
            userInfo.incrementBadgeCount();

            // then
            assertThat(userInfo.getBadgeCount()).isEqualTo(1);
        }
    }
}
