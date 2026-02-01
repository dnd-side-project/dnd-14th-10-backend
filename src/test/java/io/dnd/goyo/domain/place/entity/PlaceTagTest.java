package io.dnd.goyo.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.tag.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PlaceTagTest {

    @Nested
    @DisplayName("PlaceTag 생성 시")
    class CreatePlaceTag {

        @Test
        void 장소_정보가_null이면_예외_발생() {
            // given
            Tag tag = mock(Tag.class);

            // when & then
            assertThatThrownBy(() -> PlaceTag.of(null, tag))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("장소 정보는 필수입니다");
        }

        @Test
        void 태그_정보가_null이면_예외_발생() {
            // given
            Place place = mock(Place.class);

            // when & then
            assertThatThrownBy(() -> PlaceTag.of(place, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("태그 정보는 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given
            Place place = mock(Place.class);
            Tag tag = mock(Tag.class);

            // when
            PlaceTag placeTag = PlaceTag.of(place, tag);

            // then
            assertThat(placeTag).isNotNull();
            assertThat(placeTag.getPlace()).isEqualTo(place);
            assertThat(placeTag.getTag()).isEqualTo(tag);
        }
    }
}
