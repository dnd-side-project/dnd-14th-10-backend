package io.dnd.goyo.domain.badge.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BadgeTest {

    private static Badge.BadgeBuilder createValidBadgeBuilder() {
        return Badge.builder()
                .code("REVIEW_MASTER")
                .name("리뷰왕");
    }

    @Nested
    @DisplayName("Badge 생성 시")
    class CreateBadge {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 코드가_비어있으면_예외_발생(String code) {
            // given
            Badge.BadgeBuilder builder = createValidBadgeBuilder()
                    .code(code);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("뱃지 코드는 필수입니다");
        }

        @Test
        void 코드가_30자를_초과하면_예외_발생() {
            // given
            Badge.BadgeBuilder builder = createValidBadgeBuilder()
                    .code("a".repeat(31));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("뱃지 코드는 30자 이내여야 합니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이름이_비어있으면_예외_발생(String name) {
            // given
            Badge.BadgeBuilder builder = createValidBadgeBuilder()
                    .name(name);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("뱃지 이름은 필수입니다");
        }

        @Test
        void 이름이_30자를_초과하면_예외_발생() {
            // given
            Badge.BadgeBuilder builder = createValidBadgeBuilder()
                    .name("a".repeat(31));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("뱃지 이름은 30자 이내여야 합니다");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given & when
            Badge badge = createValidBadgeBuilder()
                    .description("리뷰를 많이 작성한 사용자에게 부여됩니다")
                    .build();

            // then
            assertThat(badge).isNotNull();
            assertThat(badge.getCode()).isEqualTo("REVIEW_MASTER");
            assertThat(badge.getName()).isEqualTo("리뷰왕");
            assertThat(badge.getDescription()).isEqualTo("리뷰를 많이 작성한 사용자에게 부여됩니다");
        }

        @Test
        void 설명은_선택_값으로_null_허용() {
            // given & when
            Badge badge = createValidBadgeBuilder()
                    .description(null)
                    .build();

            // then
            assertThat(badge).isNotNull();
            assertThat(badge.getDescription()).isNull();
        }
    }
}
