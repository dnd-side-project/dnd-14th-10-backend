package io.dnd.goyo.domain.review.repository;

import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r.place.id FROM Review r WHERE r.user.id = :userId AND r.status = 'ACTIVE' AND r.createdAt >= :since")
    List<Long> findPlaceIdsByUserIdAndStatus(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @EntityGraph(attributePaths = {"user", "place"})
    Page<Review> findAllByPlaceIdAndStatus(Long placeId, ReviewStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "place"})
    Page<Review> findAllByUserIdAndStatus(Long userId, ReviewStatus status, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.place WHERE r.id = :reviewId")
    Optional<Review> findByIdWithUserAndPlace(@Param("reviewId") Long reviewId);

}
