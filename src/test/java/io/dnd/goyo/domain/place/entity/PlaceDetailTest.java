package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlaceDetailTest {

    @Nested
    @DisplayName("PlaceDetail 생성 시")
    class CreatePlaceDetail {

        @Test
        void 장소_정보가_null이면_예외_발생() {
            // given
            Place place = null;

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 3.5, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, -1.0, 5.1, 10.0})
        void 평점이_범위를_벗어나면_예외_발생(double rating) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, rating, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("평점은 0.0 ~ 5.0 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, 5.0})
        void 평점_경계값은_허용(double rating) {
            // given
            Place place = mock(Place.class);

            // when
            PlaceDetail placeDetail = PlaceDetail.of(place, rating, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM);

            // then
            assertThat(placeDetail.getRating()).isEqualTo(rating);
        }

        @Test
        void 콘센트_점수가_null이면_예외_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 3.5, null, CrowdStatus.NORMAL, SpaceSize.MEDIUM))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("콘센트 점수는 필수입니다");
        }

        @Test
        void 혼잡도가_null이면_예외_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 3.5, OutletScore.MANY, null, SpaceSize.MEDIUM))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("혼잡도는 필수입니다");
        }

        @Test
        void 공간_크기가_null이면_예외가_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 3.5, OutletScore.MANY, CrowdStatus.NORMAL, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("공간 크기는 필수입니다");
        }

        @Test
        void wishCount는_0으로_초기화() {
            // given
            Place place = mock(Place.class);

            // when
            PlaceDetail placeDetail = PlaceDetail.of(place, 3.5, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM);

            // then
            assertThat(placeDetail.getWishCount()).isEqualTo(0);
        }
    }
}
