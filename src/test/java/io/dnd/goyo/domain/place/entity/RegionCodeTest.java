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
        @ValueSource(longs = {0L, -1L, -10000L})
        void 양수가_아니면_예외_발생(long code) {
            // when & then
            assertThatThrownBy(() -> new RegionCode(code))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("행정구역 코드는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {9999L, 1000L, 100L})
        void 코드가_5자리_미만이면_예외_발생(long code) {
            // when & then
            assertThatThrownBy(() -> new RegionCode(code))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("행정구역 코드는 5자리(시군구) 또는 10자리(법정동)여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {100000L, 123456L, 999999L, 123456789L})
        void 코드가_6자리에서_9자리면_예외_발생(long code) {
            // when & then
            assertThatThrownBy(() -> new RegionCode(code))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("행정구역 코드는 5자리(시군구) 또는 10자리(법정동)여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {10000L, 11010L, 99999L})
        void 시군구_코드_5자리_허용(long code) {
            // when
            RegionCode regionCode = new RegionCode(code);

            // then
            assertThat(regionCode.value()).isEqualTo(code);
        }

        @ParameterizedTest
        @ValueSource(longs = {1000000000L, 1101000000L, 9999999999L})
        void 법정동_코드_10자리_허용(long code) {
            // when
            RegionCode regionCode = new RegionCode(code);

            // then
            assertThat(regionCode.value()).isEqualTo(code);
        }
    }

    @Nested
    @DisplayName("시군구 코드 추출 시")
    class GetSiGunGuCode {

        @Test
        void 시군구_코드_5자리는_그대로_반환() {
            // given
            RegionCode regionCode = new RegionCode(11010L);

            // when
            int result = regionCode.getSiGunGuCode();

            // then
            assertThat(result).isEqualTo(11010);
        }

        @Test
        void 법정동_코드_10자리는_시군구_코드_5자리_추출() {
            // given
            RegionCode regionCode = new RegionCode(1101000000L);

            // when
            int result = regionCode.getSiGunGuCode();

            // then
            assertThat(result).isEqualTo(11010);
        }

        @Test
        void 다양한_법정동_코드에서_시군구_코드_추출() {
            // given & when & then
            assertThat(new RegionCode(1168012000L).getSiGunGuCode()).isEqualTo(11680); // 강남구 역삼동
            assertThat(new RegionCode(4113510100L).getSiGunGuCode()).isEqualTo(41135); // 경기도 성남시
            assertThat(new RegionCode(2611010100L).getSiGunGuCode()).isEqualTo(26110); // 부산 중구
        }
    }
}
