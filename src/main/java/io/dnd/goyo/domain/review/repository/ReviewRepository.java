package io.dnd.goyo.domain.review.repository;

import io.dnd.goyo.domain.review.entity.Review;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r.place.id FROM Review r WHERE r.user.id = :userId AND r.status = 'ACTIVE' AND r.createdAt >= :since")
    List<Long> findPlaceIdsByUserIdAndStatus(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
