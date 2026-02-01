package io.dnd.goyo.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReviewTest {

    private static Review.ReviewBuilder createValidReviewBuilder() {
        return Review.builder()
                .user(mock(User.class))
                .place(mock(Place.class))
                .rating(4)
                .outletScore(OutletScore.MANY)
                .crowdStatus(CrowdStatus.NORMAL)
                .spaceSize(SpaceSize.MEDIUM);
    }

    @Nested
    @DisplayName("Review 생성 시")
    class CreateReview {

        @Test
        void 사용자_정보가_null이면_예외_발생() {
            // given
            Review.ReviewBuilder builder = createValidReviewBuilder()
                    .user(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 정보는 필수입니다");
        }

        @Test
        void 장소_정보가_null이면_예외_발생() {
            // given
            Review.ReviewBuilder builder = createValidReviewBuilder()
                    .place(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 6, 10})
        void 평점이_범위를_벗어나면_예외_발생(int rating) {
            // given
            Review.ReviewBuilder builder = createValidReviewBuilder()
                    .rating(rating);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("평점은 1 ~ 5 사이여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5})
        void 평점_경계값은_허용(int rating) {
            // given
            Review.ReviewBuilder builder = createValidReviewBuilder()
                    .rating(rating);

            // when
            Review review = builder.build();

            // then
            assertThat(review.getRating()).isEqualTo(rating);
        }

        @Test
        void 평점은_선택_값으로_null_허용() {
            // given
            Review.ReviewBuilder builder = createValidReviewBuilder()
                    .rating(null);

            // when
            Review review = builder.build();

            // then
            assertThat(review.getRating()).isNull();
        }

        @Test
        void 정상적인_값으로_생성하면_상태는_ACTIVE() {
            // given & when
            Review review = createValidReviewBuilder().build();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.ACTIVE);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            User user = mock(User.class);
            Place place = mock(Place.class);

            // when
            Review review = Review.builder()
                    .user(user)
                    .place(place)
                    .rating(4)
                    .content("좋은 카페입니다")
                    .outletScore(OutletScore.MANY)
                    .crowdStatus(CrowdStatus.RELAX)
                    .spaceSize(SpaceSize.LARGE)
                    .build();

            // then
            assertThat(review).isNotNull();
            assertThat(review.getUser()).isEqualTo(user);
            assertThat(review.getPlace()).isEqualTo(place);
            assertThat(review.getRating()).isEqualTo(4);
            assertThat(review.getContent()).isEqualTo("좋은 카페입니다");
        }
    }

    @Nested
    @DisplayName("Review 상태 변경 시")
    class ChangeReviewStatus {

        @Test
        void 삭제하면_상태가_DELETED로_변경() {
            // given
            Review review = createValidReviewBuilder().build();

            // when
            review.delete();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
        }

        @Test
        void 숨기면_상태가_HIDDEN으로_변경() {
            // given
            Review review = createValidReviewBuilder().build();

            // when
            review.hide();

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        }
    }
}
