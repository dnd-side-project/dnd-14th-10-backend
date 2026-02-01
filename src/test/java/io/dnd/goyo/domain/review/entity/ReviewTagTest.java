package io.dnd.goyo.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.tag.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReviewTagTest {

    @Nested
    @DisplayName("ReviewTag 생성 시")
    class CreateReviewTag {

        @Test
        void 리뷰_정보가_null이면_예외_발생() {
            // given
            Tag tag = mock(Tag.class);

            // when & then
            assertThatThrownBy(() -> ReviewTag.of(null, tag))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("리뷰 정보는 필수입니다");
        }

        @Test
        void 태그_정보가_null이면_예외_발생() {
            // given
            Review review = mock(Review.class);

            // when & then
            assertThatThrownBy(() -> ReviewTag.of(review, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            Review review = mock(Review.class);
            Tag tag = mock(Tag.class);

            // when
            ReviewTag reviewTag = ReviewTag.of(review, tag);

            // then
            assertThat(reviewTag).isNotNull();
            assertThat(reviewTag.getReview()).isEqualTo(review);
            assertThat(reviewTag.getTag()).isEqualTo(tag);
        }
    }
}
