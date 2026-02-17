package io.dnd.goyo.domain.history.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class HistoryTest {

    @Nested
    @DisplayName("History 생성 시")
    class CreateHistory {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> History.of(null, place))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 공간_정보가_null이면_예외_발생() {
            // given
            User user = mock(User.class);

            // when & then
            assertThatThrownBy(() -> History.of(user, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("공간 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성하면_viewedAt이_현재_시간으로_설정() {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);
            LocalDateTime beforeCreate = LocalDateTime.now();

            // when
            History history = History.of(user, place);

            // then
            assertThat(history.getViewedAt()).isAfterOrEqualTo(beforeCreate);
            assertThat(history.getViewedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);

            // when
            History history = History.of(user, place);

            // then
            assertThat(history).isNotNull();
            assertThat(history.getUser()).isEqualTo(user);
            assertThat(history.getPlace()).isEqualTo(place);
        }
    }

    @Nested
    @DisplayName("viewedAt 업데이트 시")
    class UpdateViewedAt {

        @Test
        void viewedAt이_현재_시간으로_업데이트() throws InterruptedException {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);
            History history = History.of(user, place);
            LocalDateTime originalViewedAt = history.getViewedAt();

            Thread.sleep(10);

            // when
            history.updateViewedAt();

            // then
            assertThat(history.getViewedAt()).isAfter(originalViewedAt);
        }
    }
}
