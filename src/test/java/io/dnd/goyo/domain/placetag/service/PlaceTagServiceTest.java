package io.dnd.goyo.domain.placetag.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
        void 태그_목록이_null이면_아무_작업도_하지_않는다() {
            // when
            placeTagService.replacePlaceTags(place, null);

            // then
            verify(placeTagRepository, never()).findTagIdsByPlaceId(any());
        }

        @Test
        void 새_태그만_추가되는_경우() {
            // given
            given(placeTagRepository.findTagIdsByPlaceId(placeId)).willReturn(List.of(1L, 2L));
            given(tagRepository.findAllById(anyList())).willReturn(List.of(createTag(3L)));

            // when
            placeTagService.replacePlaceTags(place, List.of(1L, 2L, 3L));

            // then
            verify(placeTagRepository, never()).deleteByPlaceIdAndTagIdIn(any(), any());
            verify(placeTagRepository).saveAll(anyList());
        }

        @Test
        void 기존_태그만_삭제되는_경우() {
            // given
            given(placeTagRepository.findTagIdsByPlaceId(placeId)).willReturn(List.of(1L, 2L, 3L));

            // when
            placeTagService.replacePlaceTags(place, List.of(1L, 2L));

            // then
            verify(placeTagRepository).deleteByPlaceIdAndTagIdIn(eq(placeId), anyList());
            verify(placeTagRepository, never()).saveAll(any());
        }

        @Test
        void 태그_추가와_삭제가_동시에_일어나는_경우() {
            // given
            given(placeTagRepository.findTagIdsByPlaceId(placeId)).willReturn(List.of(1L, 2L));
            given(tagRepository.findAllById(anyList())).willReturn(List.of(createTag(3L)));

            // when
            placeTagService.replacePlaceTags(place, List.of(2L, 3L));

            // then
            verify(placeTagRepository).deleteByPlaceIdAndTagIdIn(eq(placeId), anyList());
            verify(placeTagRepository).saveAll(anyList());
        }

        @Test
        void 동일한_태그로_업데이트_시_변경_없음() {
            // given
            given(placeTagRepository.findTagIdsByPlaceId(placeId)).willReturn(List.of(1L, 2L));

            // when
            placeTagService.replacePlaceTags(place, List.of(1L, 2L));

            // then
            verify(placeTagRepository, never()).deleteByPlaceIdAndTagIdIn(any(), any());
            verify(placeTagRepository, never()).saveAll(any());
        }

        @Test
        void 존재하지_않는_태그_포함_시_예외_발생() {
            // given
            given(placeTagRepository.findTagIdsByPlaceId(placeId)).willReturn(List.of(1L));
            given(tagRepository.findAllById(anyList())).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> placeTagService.replacePlaceTags(place, List.of(1L, 999L)))
                    .isInstanceOf(BusinessException.class);
        }

        private Tag createTag(Long id) {
            Tag tag = mock(Tag.class);
            given(tag.getId()).willReturn(id);
            return tag;
        }
    }
}