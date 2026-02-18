package io.dnd.goyo.domain.history.service;

import io.dnd.goyo.domain.history.entity.History;
import io.dnd.goyo.domain.history.event.PlaceViewedEvent;
import io.dnd.goyo.domain.history.repository.HistoryRepository;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.service.PlaceReader;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryEventListener {

    private final HistoryRepository historyRepository;
    private final UserReader userReader;
    private final PlaceReader placeReader;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePlaceViewed(PlaceViewedEvent event) {
        Optional<History> existing = historyRepository.findByUserIdAndPlaceId(
                event.userId(), event.placeId());

        if (existing.isPresent()) {
            existing.get().updateViewedAt();
        } else {
            User user = userReader.getUser(event.userId());
            Place place = placeReader.getPlace(event.placeId());
            historyRepository.save(History.of(user, place));
        }
    }
}
