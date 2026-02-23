package io.dnd.goyo.domain.review.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReviewTagsTest {

    @Nested
    @DisplayName("ReviewTags 생성 시")
    class CreateReviewTags {

        @Test
        void 태그_목록이_null이면_예외_발생() {
            assertThatThrownBy(() -> ReviewTags.from(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 목록은 필수입니다");
        }

        @Test
        void 태그가_2개_미만이면_예외_발생() {
            // given
            List<Long> tagIds = createTagIds(1);

            // when & then
            assertThatThrownBy(() -> ReviewTags.from(tagIds))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그는 2~5개 선택해야 합니다");
        }

        @Test
        void 태그가_5개_초과면_예외_발생() {
            // given
            List<Long> tagIds = createTagIds(6);

            // when & then
            assertThatThrownBy(() -> ReviewTags.from(tagIds))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그는 2~5개 선택해야 합니다");
        }

        @Test
        void 태그_목록에_null이_포함되면_무시하고_생성() {
            // given
            List<Long> tagIds = new ArrayList<>(createTagIds(2));
            tagIds.add(null);

            // when
            ReviewTags reviewTags = ReviewTags.from(tagIds);

            // then
            assertThat(reviewTags).isNotNull();
            assertThat(reviewTags.tagIds()).hasSize(2);
        }

        @Test
        void 중복된_태그가_포함되면_제거하고_생성() {
            // given
            List<Long> tagIds = List.of(1L, 1L, 2L);

            // when
            ReviewTags reviewTags = ReviewTags.from(tagIds);

            // then
            assertThat(reviewTags).isNotNull();
            assertThat(reviewTags.tagIds()).hasSize(2);
            assertThat(reviewTags.tagIds()).containsExactly(1L, 2L);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            List<Long> tagIds = createTagIds(3);

            // when
            ReviewTags reviewTags = ReviewTags.from(tagIds);

            // then
            assertThat(reviewTags).isNotNull();
            assertThat(reviewTags.tagIds()).hasSize(3);
        }
    }

    private List<Long> createTagIds(int count) {
        return LongStream.rangeClosed(1, count)
                .boxed()
                .toList();
    }
}
