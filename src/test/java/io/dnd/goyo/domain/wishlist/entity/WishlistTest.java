package io.dnd.goyo.domain.wishlist.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WishlistTest {

    @Nested
    @DisplayName("Wishlist 생성 시")
    class CreateWishlist {

        @Test
        void 사용자가_null이면_예외_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> Wishlist.of(null, place))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보가 누락되었습니다.");
        }

        @Test
        void 장소_정보가_null이면_예외_발생() {
            // given
            User user = mock(User.class);

            // when & then
            assertThatThrownBy(() -> Wishlist.of(user, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보가 누락되었습니다.");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);

            // when
            Wishlist wishlist = Wishlist.of(user, place);

            // then
            assertThat(wishlist).isNotNull();
            assertThat(wishlist.getUser()).isEqualTo(user);
            assertThat(wishlist.getPlace()).isEqualTo(place);
        }
    }

}
