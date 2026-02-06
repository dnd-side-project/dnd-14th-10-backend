package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
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
            assertThatThrownBy(() -> PlaceDetail.of(place, 50, 50, 50, 50))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 101})
        void 콘센트_점수가_범위를_벗어나면_예외_발생(int score) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, score, 50, 50, 50))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("콘센트 점수는 0~100 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 101})
        void 혼잡도_점수가_범위를_벗어나면_예외_발생(int score) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 50, score, 50, 50))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("혼잡도 점수는 0~100 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 101})
        void 공간_크기_점수가_범위를_벗어나면_예외_발생(int score) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 50, 50, score, 50))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("공간 크기 점수는 0~100 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 101})
        void 분위기_점수가_범위를_벗어나면_예외_발생(int score) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 50, 50, 50, score))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("분위기 점수는 0~100 사이여야 합니다");
        }

        @Test
        void 초기_값들이_정상적으로_설정된다() {
            // given
            Place place = mock(Place.class);
            int outletScore = 25;
            int crowdScore = 50;
            int spaceSizeScore = 75;
            int quietScore = 100;

            // when
            PlaceDetail placeDetail = PlaceDetail.of(place, outletScore, crowdScore, spaceSizeScore, quietScore);

            // then
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(outletScore);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(crowdScore);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(spaceSizeScore);
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(quietScore);
            assertThat(placeDetail.getTotalRating()).isEqualTo(0.0);
            assertThat(placeDetail.getReviewCount()).isEqualTo(0);
            assertThat(placeDetail.getWishCount()).isEqualTo(0);
        }
    }
}
