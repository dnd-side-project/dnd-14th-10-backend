package io.dnd.goyo.domain.history.service;

import io.dnd.goyo.domain.history.dto.response.HistoryItemResponse;
import io.dnd.goyo.domain.history.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final HistoryRepository historyRepository;

    public Page<HistoryItemResponse> getMyHistories(Long userId, Pageable pageable) {
        return historyRepository.findAllByUserId(userId, pageable)
                .map(HistoryItemResponse::from);
    }
}
