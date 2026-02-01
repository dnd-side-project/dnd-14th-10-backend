package io.dnd.goyo.domain.place.entity;

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

class PlaceImageTest {

    @Nested
    @DisplayName("PlaceImage 생성 시")
    class CreatePlaceImage {

        @Test
        void 장소_정보가_null이면_예외_발생() {
            // given
            Place place = null;

            // when & then
            assertThatThrownBy(() -> PlaceImage.of(place, "https://example.com/image.jpg", true, 1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보는 필수입니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void 이미지_URL이_비어있으면_예외_발생(String imageUrl) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceImage.of(place, imageUrl, true, 1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 URL은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        void 이미지_순서가_음수이면_예외_발생(int sequence) {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceImage.of(place, "https://example.com/image.jpg", true, sequence))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 순서는 0 이상이어야 합니다");
        }

        @Test
        void sequence_0은_허용() {
            // given
            Place place = mock(Place.class);

            // when
            PlaceImage placeImage = PlaceImage.of(place, "https://example.com/image.jpg", true, 0);

            // then
            assertThat(placeImage.getSequence()).isEqualTo(0);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            Place place = mock(Place.class);

            // when
            PlaceImage placeImage = PlaceImage.of(place, "https://example.com/image.jpg", true, 1);

            // then
            assertThat(placeImage).isNotNull();
        }
    }
}
