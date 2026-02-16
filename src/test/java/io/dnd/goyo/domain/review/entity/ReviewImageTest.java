package io.dnd.goyo.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ReviewImageTest {

    @Nested
    @DisplayName("ReviewImage 생성 시")
    class CreateReviewImage {

        @Test
        void 리뷰_정보가_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> ReviewImage.of(null, "https://example.com/image.jpg", 0, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("리뷰 정보는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이미지_URL이_비어있으면_예외_발생(String imageUrl) {
            // given
            Review review = mock(Review.class);

            // when & then
            assertThatThrownBy(() -> ReviewImage.of(review, imageUrl, 0, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 URL은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10})
        void 이미지_순서가_음수이면_예외_발생(int sequence) {
            // given
            Review review = mock(Review.class);

            // when & then
            assertThatThrownBy(() -> ReviewImage.of(review, "https://example.com/image.jpg", sequence, false))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 순서는 0 이상이어야 합니다");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            Review review = mock(Review.class);
            String imageUrl = "https://example.com/image.jpg";
            int sequence = 0;

            // when
            ReviewImage reviewImage = ReviewImage.of(review, imageUrl, sequence, false);

            // then
            assertThat(reviewImage).isNotNull();
            assertThat(reviewImage.getReview()).isEqualTo(review);
            assertThat(reviewImage.getImageUrl()).isEqualTo(imageUrl);
            assertThat(reviewImage.getSequence()).isEqualTo(sequence);
            assertThat(reviewImage.isPrimary()).isFalse();
        }

        @Test
        void isPrimary_true로_생성_성공() {
            // given
            Review review = mock(Review.class);

            // when
            ReviewImage reviewImage = ReviewImage.of(review, "https://example.com/image.jpg", 0, true);

            // then
            assertThat(reviewImage.isPrimary()).isTrue();
        }
    }
}
