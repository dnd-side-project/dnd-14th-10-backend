package io.dnd.goyo.domain.placetag.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.placetag.repository.PlaceTagRepository;
import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.repository.TagRepository;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceTagServiceTest {

    @InjectMocks
    private PlaceTagService placeTagService;

    @Mock
    private PlaceTagRepository placeTagRepository;

    @Mock
    private TagRepository tagRepository;

    @Nested
    @DisplayName("태그 등록 시")
    class RegisterPlaceTags {

        private Place place;

        @BeforeEach
        void setUp() {
            place = mock(Place.class);
        }

        @Test
        void 정상적으로_태그가_등록된다() {
            // given
            List<Long> tagIds = createTagIds(3);
            given(tagRepository.findAllById(tagIds)).willReturn(createTags(3));

            // when
            placeTagService.registerPlaceTags(place, tagIds);

            // then
            verify(placeTagRepository).saveAll(anyList());
        }

        @Test
        void 존재하지_않는_태그_포함_시_예외_발생() {
            // given
            List<Long> tagIds = createTagIds(3);
            given(tagRepository.findAllById(tagIds)).willReturn(createTags(2));

            // when & then
            assertThatThrownBy(() -> placeTagService.registerPlaceTags(place, tagIds))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("존재하지 않는 태그");
        }
    }

    @Nested
    @DisplayName("태그 교체 시")
    class ReplacePlaceTags {

        private final Long placeId = 1L;
        private Place place;

        @BeforeEach
        void setUp() {
            place = mock(Place.class);
            given(place.getId()).willReturn(placeId);
        }

        @Test
        void 기존_태그_삭제_후_새_태그가_등록된다() {
            // given
            List<Long> tagIds = createTagIds(3);
            given(tagRepository.findAllById(tagIds)).willReturn(createTags(3));

            // when
            placeTagService.replacePlaceTags(place, tagIds);

            // then
            verify(placeTagRepository).deleteAllByPlaceId(placeId);
            verify(placeTagRepository).saveAll(anyList());
        }

        @Test
        void 존재하지_않는_태그_포함_시_예외_발생() {
            // given
            List<Long> tagIds = createTagIds(3);
            given(tagRepository.findAllById(tagIds)).willReturn(createTags(2));

            // when & then
            assertThatThrownBy(() -> placeTagService.replacePlaceTags(place, tagIds))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("존재하지 않는 태그");
        }

        @Test
        void 태그_목록이_null이면_예외_발생() {
            // when & then
            assertThatThrownBy(() -> placeTagService.replacePlaceTags(place, null))
                    .isInstanceOf(BusinessException.class);
        }
    }

    private List<Long> createTagIds(int count) {
        return LongStream.rangeClosed(1, count)
                .boxed()
                .toList();
    }

    private List<Tag> createTags(int count) {
        return LongStream.rangeClosed(1, count)
                .mapToObj(i -> mock(Tag.class))
                .toList();
    }
}