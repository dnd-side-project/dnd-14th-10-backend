package io.dnd.goyo.domain.history.repository;

import io.dnd.goyo.domain.history.entity.History;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoryRepository extends JpaRepository<History, Long> {

    Optional<History> findByUserIdAndPlaceId(Long userId, Long placeId);

    @Query(value = "SELECT h FROM History h JOIN FETCH h.place p LEFT JOIN FETCH p.placeDetail WHERE h.user.id = :userId AND p.status <> 'DELETED'",
           countQuery = "SELECT COUNT(h) FROM History h JOIN h.place p WHERE h.user.id = :userId AND p.status <> 'DELETED'")
    Page<History> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
