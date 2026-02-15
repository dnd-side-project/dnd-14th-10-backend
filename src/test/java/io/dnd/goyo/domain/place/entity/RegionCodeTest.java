package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RegionCodeTest {

    @Nested
    @DisplayName("RegionCode 생성 시")
    class CreateRegionCode {

        @Test
        void 코드가_null이면_예외_발생() {
            // given
            Long code = null;

            // when & then
            assertThatThrownBy(() -> new RegionCode(code))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("행정구역 코드는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {9999L, 1000L, 100L, 0L, -1L})
        void 코드가_5자리_미만이면_예외_발생(long code) {
            // when & then
            assertThatThrownBy(() -> new RegionCode(code))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("행정구역 코드는 5~10자리여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {10000L, 99999L, 1111010100L, 9999999999L})
        void 경계값은_허용(long code) {
            // when
            RegionCode regionCode = new RegionCode(code);

            // then
            assertThat(regionCode.value()).isEqualTo(code);
        }

        @Test
        void 정상적인_코드로_생성_성공() {
            // given
            long code = 1111010100L;

            // when
            RegionCode regionCode = new RegionCode(code);

            // then
            assertThat(regionCode.value()).isEqualTo(code);
        }
    }
}
