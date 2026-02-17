package io.dnd.goyo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserStatsTest {

    @Nested
    @DisplayName("UserStats 생성 시")
    class CreateUserStats {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> UserStats.of(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성하면_카운트는_모두_0() {
            // given
            User user = mock(User.class);

            // when
            UserStats userStats = UserStats.of(user);

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(0);
            assertThat(userStats.getPlaceCount()).isEqualTo(0);
            assertThat(userStats.getBadgeCount()).isEqualTo(0);
            assertThat(userStats.getImageCount()).isEqualTo(0);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);

            // when
            UserStats userStats = UserStats.of(user);

            // then
            assertThat(userStats).isNotNull();
            assertThat(userStats.getUser()).isEqualTo(user);
        }
    }

    @Nested
    @DisplayName("카운트 증가 시")
    class IncrementCount {

        @Test
        void 리뷰_카운트_증가() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.incrementReviewCount();

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(1);
        }

        @Test
        void 장소_카운트_증가() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.incrementPlaceCount();

            // then
            assertThat(userStats.getPlaceCount()).isEqualTo(1);
        }

        @Test
        void 뱃지_카운트_증가() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.incrementBadgeCount();

            // then
            assertThat(userStats.getBadgeCount()).isEqualTo(1);
        }

        @Test
        void 이미지_카운트_증가() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.incrementImageCount();

            // then
            assertThat(userStats.getImageCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("카운트 감소 시")
    class DecrementCount {

        @Test
        void 리뷰_카운트_감소() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);
            userStats.incrementReviewCount();

            // when
            userStats.decrementReviewCount();

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(0);
        }

        @Test
        void 장소_카운트_감소() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);
            userStats.incrementPlaceCount();

            // when
            userStats.decrementPlaceCount();

            // then
            assertThat(userStats.getPlaceCount()).isEqualTo(0);
        }

        @Test
        void 이미지_카운트_감소() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);
            userStats.incrementImageCount();

            // when
            userStats.decrementImageCount();

            // then
            assertThat(userStats.getImageCount()).isEqualTo(0);
        }

        @Test
        void 리뷰_카운트_0에서_감소해도_0_유지() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.decrementReviewCount();

            // then
            assertThat(userStats.getReviewCount()).isEqualTo(0);
        }

        @Test
        void 장소_카운트_0에서_감소해도_0_유지() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.decrementPlaceCount();

            // then
            assertThat(userStats.getPlaceCount()).isEqualTo(0);
        }

        @Test
        void 이미지_카운트_0에서_감소해도_0_유지() {
            // given
            User user = mock(User.class);
            UserStats userStats = UserStats.of(user);

            // when
            userStats.decrementImageCount();

            // then
            assertThat(userStats.getImageCount()).isEqualTo(0);
        }
    }
}
