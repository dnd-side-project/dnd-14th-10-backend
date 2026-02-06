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

class PlaceImageTest {

    @Nested
    @DisplayName("PlaceImage 생성 시")
    class CreatePlaceImage {

        @ParameterizedTest
        @NullAndEmptySource
        void 이미지_키가_비어있으면_예외_발생(String imageKey) {
            assertThatThrownBy(() -> PlaceImage.of(imageKey, true, 1))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 키는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100})
        void 이미지_순서가_음수이면_예외_발생(int sequence) {
            assertThatThrownBy(() -> PlaceImage.of("place/uuid.jpg", true, sequence))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미지 순서는 0 이상이어야 합니다");
        }

        @Test
        void sequence_0은_허용() {
            PlaceImage placeImage = PlaceImage.of("place/uuid.jpg", true, 0);

            assertThat(placeImage.getSequence()).isEqualTo(0);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            PlaceImage placeImage = PlaceImage.of("place/uuid.jpg", true, 1);

            assertThat(placeImage).isNotNull();
            assertThat(placeImage.getImageKey()).isEqualTo("place/uuid.jpg");
            assertThat(placeImage.isRepresentativeFlag()).isTrue();
            assertThat(placeImage.getSequence()).isEqualTo(1);
        }
    }
}
