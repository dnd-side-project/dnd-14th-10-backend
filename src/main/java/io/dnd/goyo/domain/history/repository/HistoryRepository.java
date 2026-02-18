package io.dnd.goyo.domain.history.repository;

import io.dnd.goyo.domain.history.entity.History;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {

    Optional<History> findByUserIdAndPlaceId(Long userId, Long placeId);

    @EntityGraph(attributePaths = {"place", "place.placeDetail"})
    Page<History> findAllByUserId(Long userId, Pageable pageable);
}
