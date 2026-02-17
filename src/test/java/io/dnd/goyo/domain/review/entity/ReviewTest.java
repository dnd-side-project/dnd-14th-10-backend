package io.dnd.goyo.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import java.time.LocalDateTime;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReviewTest {

    private static Review createValidReview() {
        return Review.create(mock(User.class), mock(Place.class), 4.0, Mood.CALM,
                OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null);
    }

    @Nested
    @DisplayName("Review 생성 시")
    class CreateReview {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Review.create(null, mock(Place.class), 4.0, Mood.CALM,
                    OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 공간_정보가_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Review.create(mock(User.class), null, 4.0, Mood.CALM,
                    OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("공간 정보는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -0.5, 5.5, 10.0})
        void 평점이_범위를_벗어나면_예외_발생(double rating) {
            // when & then
            assertThatThrownBy(() -> Review.create(mock(User.class), mock(Place.class), rating,
                    Mood.CALM, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("평점은 0.5 ~ 5.0 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.5, 5.0})
        void 평점_경계값은_허용(double rating) {
            // when
            Review review = Review.create(mock(User.class), mock(Place.class), rating,
                    Mood.CALM, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null);

            // then
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        void 평점은_선택_값으로_null_허용() {
            // when
            Review review = Review.create(mock(User.class), mock(Place.class), null,
                    Mood.CALM, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null);

            // then
            assertThat(review.getRating()).isNull();
        }

        @Test
        void 정상적인_값으로_생성하면_상태는_ACTIVE() {
            // given & when
            Review review = createValidReview();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.ACTIVE);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);

            // when
            Review review = Review.create(user, place, 4.0, null,
                    OutletScore.MANY, CrowdStatus.RELAX, SpaceSize.LARGE,
                    "좋은 카페입니다", null);

            // then
            assertThat(review).isNotNull();
            assertThat(review.getUser()).isEqualTo(user);
            assertThat(review.getPlace()).isEqualTo(place);
            assertThat(review.getRating()).isEqualTo(4.0);
            assertThat(review.getContent()).isEqualTo("좋은 카페입니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.7, 1.2, 2.7, 4.9})
        void 평점이_0점5_단위가_아니면_예외_발생(double rating) {
            // when & then
            assertThatThrownBy(() -> Review.create(mock(User.class), mock(Place.class), rating,
                    Mood.CALM, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("평점은 0.5 단위로 입력해야 합니다");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0})
        void 유효한_0점5_단위_평점은_허용(double rating) {
            // when
            Review review = Review.create(mock(User.class), mock(Place.class), rating,
                    Mood.CALM, OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM, null, null);

            // then
            assertThat(review.getRating()).isEqualTo(rating);
        }
    }

    @Nested
    @DisplayName("Review 상태 변경 시")
    class ChangeReviewStatus {

        @Test
        void 삭제하면_상태가_DELETED로_변경() {
            // given
            Review review = createValidReview();

            // when
            review.delete();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
        }

        @Test
        void 숨기면_상태가_HIDDEN으로_변경() {
            // given
            Review review = createValidReview();

            // when
            review.hide();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        }
    }

    @Nested
    @DisplayName("Review 수정 시")
    class UpdateReview {

        @Test
        void 정상적인_값으로_수정_성공() {
            // given
            Review review = createValidReview();

            // when
            review.update(5.0, Mood.SILENT, OutletScore.FEW, CrowdStatus.RELAX,
                    SpaceSize.LARGE, "수정된 내용", LocalDateTime.of(2026, 1, 1, 12, 0));

            // then
            assertThat(review.getRating()).isEqualTo(5.0);
            assertThat(review.getMood()).isEqualTo(Mood.SILENT);
            assertThat(review.getOutletScore()).isEqualTo(OutletScore.FEW);
            assertThat(review.getCrowdStatus()).isEqualTo(CrowdStatus.RELAX);
            assertThat(review.getSpaceSize()).isEqualTo(SpaceSize.LARGE);
            assertThat(review.getContent()).isEqualTo("수정된 내용");
            assertThat(review.getVisitedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0));
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -0.5, 5.5, 10.0})
        void 잘못된_평점으로_수정_시_예외_발생(double rating) {
            // given
            Review review = createValidReview();

            // when & then
            assertThatThrownBy(() -> review.update(rating, Mood.CALM, OutletScore.MANY,
                    CrowdStatus.NORMAL, SpaceSize.MEDIUM, "내용", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("평점은 0.5 ~ 5.0 사이여야 합니다");
        }
    }
}
