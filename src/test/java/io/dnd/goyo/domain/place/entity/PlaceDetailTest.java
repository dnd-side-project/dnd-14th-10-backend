package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.ReviewScores;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
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
        void 공간_정보가_null이면_예외_발생() {
            // given
            Place place = null;

            // when & then
            assertThatThrownBy(() -> PlaceDetail.of(place, 50, 50, 50, 50))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("공간 정보는 필수입니다");
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

    @Nested
    @DisplayName("찜 수 관리 시")
    class WishCountManagement {

        @Test
        void 찜_수_증가_성공() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);

            // when
            placeDetail.incrementWishCount();

            // then
            assertThat(placeDetail.getWishCount()).isEqualTo(1);
        }

        @Test
        void 찜_수_감소_성공() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);
            placeDetail.incrementWishCount();

            // when
            placeDetail.decrementWishCount();

            // then
            assertThat(placeDetail.getWishCount()).isEqualTo(0);
        }

        @Test
        void 찜_수가_0일_때_감소_시_음수_방지() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);

            // when
            placeDetail.decrementWishCount();

            // then
            assertThat(placeDetail.getWishCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("리뷰 점수 관리 시")
    class ReviewScoreManagement {

        @Test
        void 리뷰_점수_추가_성공() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);

            // when
            placeDetail.addReviewScores(new ReviewScores(4.0, 80, 60, 70, 90));

            // then
            assertThat(placeDetail.getTotalRating()).isEqualTo(4.0);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(130);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(110);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(120);
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(140);
            assertThat(placeDetail.getReviewCount()).isEqualTo(1);
        }

        @Test
        void 리뷰_점수_제거_성공() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);
            placeDetail.addReviewScores(new ReviewScores(4.0, 80, 60, 70, 90));

            // when
            placeDetail.removeReviewScores(new ReviewScores(4.0, 80, 60, 70, 90));

            // then
            assertThat(placeDetail.getTotalRating()).isEqualTo(0.0);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(50);
            assertThat(placeDetail.getReviewCount()).isEqualTo(0);
        }

        @Test
        void 리뷰가_없을_때_점수_제거_시_무시된다() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);

            // when
            placeDetail.removeReviewScores(new ReviewScores(4.0, 80, 60, 70, 90));

            // then
            assertThat(placeDetail.getReviewCount()).isEqualTo(0);
            assertThat(placeDetail.getTotalRating()).isEqualTo(0.0);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(50);
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(50);
        }

        @Test
        void 리뷰_점수_연속_추가_성공() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 50, 50, 50, 50);

            // when
            placeDetail.addReviewScores(new ReviewScores(4.0, 80, 60, 70, 90));
            placeDetail.addReviewScores(new ReviewScores(3.0, 40, 50, 60, 75));

            // then
            assertThat(placeDetail.getTotalRating()).isEqualTo(7.0);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(170);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(160);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(180);
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(215);
            assertThat(placeDetail.getReviewCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("공간 점수 수정 시")
    class UpdateScore {

        @Test
        void 분위기만_변경하면_차분함_지수만_바뀐다() {
            // given: CALM(75)으로 초기화
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 100, 0, 100, 75);

            // when
            placeDetail.updateScore(Mood.SILENT, null, null, null);

            // then: mood만 SILENT(100)으로 변경
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(100);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(100);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(0);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(100);
        }

        @Test
        void null_전달_시_점수_변경_없음() {
            // given
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 100, 0, 100, 75);

            // when
            placeDetail.updateScore(null, null, null, null);

            // then
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(75);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(100);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(0);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(100);
        }

        @Test
        void 모든_필드_동시_변경() {
            // given: MANY(100), RELAX(0), LARGE(100), CALM(75)으로 초기화
            Place place = mock(Place.class);
            PlaceDetail placeDetail = PlaceDetail.of(place, 100, 0, 100, 75);

            // when: FEW(0), FULL(100), SMALL(0), SILENT(100)으로 변경
            placeDetail.updateScore(Mood.SILENT, SpaceSize.SMALL, OutletScore.FEW, CrowdStatus.FULL);

            // then
            assertThat(placeDetail.getTotalQuietScore()).isEqualTo(100);
            assertThat(placeDetail.getTotalSpaceSizeScore()).isEqualTo(0);
            assertThat(placeDetail.getTotalOutletScore()).isEqualTo(0);
            assertThat(placeDetail.getTotalCrowdScore()).isEqualTo(100);
        }
    }
}
