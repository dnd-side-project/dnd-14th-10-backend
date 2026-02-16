package io.dnd.goyo.domain.review.repository;

import io.dnd.goyo.domain.review.dto.TagWithCreatedAtDto;
import io.dnd.goyo.domain.review.entity.ReviewTag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

    @Query("SELECT new io.dnd.goyo.domain.review.dto.TagWithCreatedAtDto(rt.tag.id, rt.review.createdAt) "
            + "FROM ReviewTag rt "
            + "WHERE rt.review.user.id = :userId AND rt.review.status = 'ACTIVE' "
            + "AND rt.review.createdAt >= :since")
    List<TagWithCreatedAtDto> findTagsWithCreatedAt(@Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    List<ReviewTag> findAllByReviewId(Long reviewId);

    List<ReviewTag> findAllByReviewIdIn(List<Long> reviewIds);

    @Modifying
    @Query("DELETE FROM ReviewTag rt WHERE rt.review.id = :reviewId")
    void deleteAllByReviewId(@Param("reviewId") Long reviewId);
}
