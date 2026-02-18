package io.dnd.goyo.domain.tag.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.tag.enums.TagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TagTest {

    @Nested
    @DisplayName("Tag 생성 시")
    class CreateTag {

        @Test
        void 타입이_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> Tag.of(null, "조용한"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 타입은 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이름이_비어있으면_예외_발생(String name) {
            // when & then
            assertThatThrownBy(() -> Tag.of(TagType.PLACE, name))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 이름은 필수입니다");
        }

        @Test
        void 이름이_30자를_초과하면_예외_발생() {
            // given
            String name = "a".repeat(31);

            // when & then
            assertThatThrownBy(() -> Tag.of(TagType.PLACE, name))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 이름은 30자 이내여야 합니다");
        }

        @Test
        void 공간_태그_정상적인_값으로_생성_가능() {
            // given & when
            Tag tag = Tag.of(TagType.PLACE, "조용한");

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getType()).isEqualTo(TagType.PLACE);
            assertThat(tag.getName()).isEqualTo("조용한");
        }

        @Test
        void 리뷰_태그_정상적인_값으로_생성_가능() {
            // given & when
            Tag tag = Tag.of(TagType.REVIEW, "디저트가 맛있어요");

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getType()).isEqualTo(TagType.REVIEW);
            assertThat(tag.getName()).isEqualTo("디저트가 맛있어요");
        }

        @Test
        void code_포함하여_생성_가능() {
            // given & when
            Tag tag = Tag.of(TagType.REVIEW, "CLEAN", "청결해요");

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getType()).isEqualTo(TagType.REVIEW);
            assertThat(tag.getCode()).isEqualTo("CLEAN");
            assertThat(tag.getName()).isEqualTo("청결해요");
        }

        @Test
        void code가_null이어도_생성_가능() {
            // given & when
            Tag tag = Tag.of(TagType.REVIEW, null, "청결해요");

            // then
            assertThat(tag).isNotNull();
            assertThat(tag.getCode()).isNull();
            assertThat(tag.getName()).isEqualTo("청결해요");
        }
    }
}
