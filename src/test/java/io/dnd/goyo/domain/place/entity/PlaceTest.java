package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

class PlaceTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final Point DEFAULT_LOCATION = createPoint(127.0, 37.5);

    private static Point createPoint(double lng, double lat) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    private static Place.PlaceBuilder createValidPlaceBuilder() {
        return Place.builder()
                .name("테스트 카페")
                .category(PlaceCategory.CAFE)
                .location(DEFAULT_LOCATION)
                .regionCode(11111)
                .addressDetail("청계천로 100번길 31")
                .userId(1L);
    }

    @Nested
    @DisplayName("Place 생성 시")
    class CreatePlace {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이름이_비어있으면_예외_발생(String name) {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .name(name);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 이름은 필수입니다");
        }

        @Test
        void 이름이_50자를_초과하면_예외_발생() {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .name("a".repeat(51));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 이름은 50자 이내여야 합니다");
        }

        @Test
        void 카테고리가_null이면_예외_발생() {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .category(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("카테고리는 필수입니다");
        }

        @Test
        void 위치_정보가_null이면_예외_발생() {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .location(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("위치 정보는 필수입니다");
        }

        @ParameterizedTest
        @MethodSource("invalidCoordinates")
        void 좌표_범위를_벗어나면_예외_발생(double lng, double lat) {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .location(createPoint(lng, lat));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("유효한 대한민국 좌표가 아닙니다");
        }

        static Stream<Arguments> invalidCoordinates() {
            return Stream.of(
                    Arguments.of(127.0, 50.0),
                    Arguments.of(127.0, 32.0),
                    Arguments.of(140.0, 37.5),
                    Arguments.of(120.0, 37.5)
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 상세_주소가_비어있으면_예외_발생(String addressDetail) {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .addressDetail(addressDetail);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("상세 주소는 필수입니다");
        }

        @Test
        void 상세_주소가_50자를_초과하면_예외_발생() {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .addressDetail("a".repeat(51));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("상세 주소는 50자 이내여야 합니다");
        }

        @Test
        void 등록자_ID가_null이면_예외_발생() {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder().userId(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("등록자 ID는 필수입니다");
        }

        @ParameterizedTest
        @MethodSource("invalidOperatingHours")
        void 영업시간은_시작과_종료를_함께_입력하지_않으면_예외_발생(LocalTime openTime, LocalTime closeTime) {
            // given
            Place.PlaceBuilder builder = createValidPlaceBuilder()
                    .openTime(openTime)
                    .closeTime(closeTime);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("영업시간은 시작과 종료를 함께 입력해야 합니다");
        }

        static Stream<Arguments> invalidOperatingHours() {
            return Stream.of(
                    Arguments.of(LocalTime.of(9, 0), null),
                    Arguments.of(null, LocalTime.of(22, 0))
            );
        }
    }
}
