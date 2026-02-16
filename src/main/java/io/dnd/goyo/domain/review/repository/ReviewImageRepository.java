package io.dnd.goyo.domain.review.repository;

import io.dnd.goyo.domain.review.entity.ReviewImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    @Modifying
    @Query("DELETE FROM ReviewImage ri WHERE ri.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") Long reviewId);

    List<ReviewImage> findAllByReviewIdOrderBySequence(Long reviewId);

    List<ReviewImage> findAllByReviewIdInOrderBySequence(List<Long> reviewIds);
}
