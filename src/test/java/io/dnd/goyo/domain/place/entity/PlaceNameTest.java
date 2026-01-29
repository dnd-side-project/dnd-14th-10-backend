package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PlaceNameTest {

    @Nested
    @DisplayName("PlaceName 생성 시")
    class CreatePlaceName {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이름이_비어있으면_예외_발생(String name) {
            // when & then
            assertThatThrownBy(() -> new PlaceName(name))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 이름은 필수입니다");
        }

        @Test
        void 이름이_50자를_초과하면_예외_발생() {
            // given
            String longName = "a".repeat(51);

            // when & then
            assertThatThrownBy(() -> new PlaceName(longName))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 이름은 50자 이내여야 합니다");
        }

        @Test
        void 이름이_50자면_생성_성공() {
            // given
            String name = "a".repeat(50);

            // when
            PlaceName placeName = new PlaceName(name);

            // then
            assertThat(placeName.value()).isEqualTo(name);
        }

        @Test
        void 정상적인_이름으로_생성_성공() {
            // given
            String name = "스타벅스 강남점";

            // when
            PlaceName placeName = new PlaceName(name);

            // then
            assertThat(placeName.value()).isEqualTo(name);
        }
    }
}
