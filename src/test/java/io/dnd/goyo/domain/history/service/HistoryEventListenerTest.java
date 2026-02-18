package io.dnd.goyo.domain.history.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.domain.history.entity.History;
import io.dnd.goyo.domain.history.event.PlaceViewedEvent;
import io.dnd.goyo.domain.history.repository.HistoryRepository;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.service.PlaceReader;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistoryEventListenerTest {

    @InjectMocks
    private HistoryEventListener historyEventListener;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private PlaceReader placeReader;

    @Nested
    @DisplayName("공간 조회 이벤트 처리 시")
    class HandlePlaceViewed {

        @Test
        void 기존_기록이_없으면_새로_생성() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            User user = mock(User.class);
            Place place = mock(Place.class);

            given(historyRepository.findByUserIdAndPlaceId(userId, placeId))
                    .willReturn(Optional.empty());
            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId)).willReturn(place);

            PlaceViewedEvent event = new PlaceViewedEvent(userId, placeId);

            // when
            historyEventListener.handlePlaceViewed(event);

            // then
            verify(historyRepository).save(any(History.class));
        }

        @Test
        void 기존_기록이_있으면_viewedAt_갱신() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            History history = mock(History.class);

            given(historyRepository.findByUserIdAndPlaceId(userId, placeId))
                    .willReturn(Optional.of(history));

            PlaceViewedEvent event = new PlaceViewedEvent(userId, placeId);

            // when
            historyEventListener.handlePlaceViewed(event);

            // then
            verify(history).updateViewedAt();
            verify(historyRepository, never()).save(any(History.class));
        }

        @Test
        void 기존_기록이_있으면_UserReader_PlaceReader_호출하지_않음() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            History history = mock(History.class);

            given(historyRepository.findByUserIdAndPlaceId(userId, placeId))
                    .willReturn(Optional.of(history));

            PlaceViewedEvent event = new PlaceViewedEvent(userId, placeId);

            // when
            historyEventListener.handlePlaceViewed(event);

            // then
            verify(userReader, never()).getUser(any());
            verify(placeReader, never()).getPlace(any());
        }
    }
}
