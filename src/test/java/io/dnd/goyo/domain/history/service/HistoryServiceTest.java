package io.dnd.goyo.domain.history.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.dnd.goyo.domain.history.dto.response.HistoryItemResponse;
import io.dnd.goyo.domain.history.entity.History;
import io.dnd.goyo.domain.history.repository.HistoryRepository;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.RegionCode;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @InjectMocks
    private HistoryService historyService;

    @Mock
    private HistoryRepository historyRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    @DisplayName("내 조회 기록 조회")
    class GetMyHistories {

        @Test
        void 조회_기록이_있으면_정상_반환() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            History history = createMockHistory(1L, 10L);
            Page<History> historyPage = new PageImpl<>(List.of(history));

            given(historyRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(historyPage);

            // when
            Page<HistoryItemResponse> result = historyService.getMyHistories(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().historyId()).isEqualTo(1L);
            assertThat(result.getContent().getFirst().placeId()).isEqualTo(10L);
        }

        @Test
        void 조회_기록이_없으면_빈_페이지_반환() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            Page<History> emptyPage = new PageImpl<>(List.of());

            given(historyRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                    .willReturn(emptyPage);

            // when
            Page<HistoryItemResponse> result = historyService.getMyHistories(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        private History createMockHistory(Long historyId, Long placeId) {
            Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));

            PlaceDetail placeDetail = mock(PlaceDetail.class);

            Place place = mock(Place.class);
            given(place.getId()).willReturn(placeId);
            given(place.getName()).willReturn("테스트 카페");
            given(place.getCategory()).willReturn(PlaceCategory.CAFE);
            given(place.getAddressDetail()).willReturn("서울시 강남구");
            given(place.getRegionCode()).willReturn(new RegionCode(1168010100L));
            given(place.getRepresentativeImageKey()).willReturn("image.jpg");
            given(place.getLocation()).willReturn(location);
            given(place.getPlaceDetail()).willReturn(placeDetail);

            History history = mock(History.class);
            given(history.getId()).willReturn(historyId);
            given(history.getPlace()).willReturn(place);
            given(history.getViewedAt()).willReturn(LocalDateTime.now());

            return history;
        }
    }
}
