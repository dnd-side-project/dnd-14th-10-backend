package io.dnd.goyo.domain.wishlist.repository;

import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w.place.id FROM Wishlist w WHERE w.user.id = :userId AND w.place.id IN :placeIds")
    List<Long> findPlaceIdsByUserIdAndPlaceIds(@Param("userId") Long userId,
            @Param("placeIds") List<Long> placeIds);

    @Query("SELECT w.place.id FROM Wishlist w WHERE w.user.id = :userId AND w.createdAt >= :since")
    List<Long> findPlaceIdsByUserId(@Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    @Query("SELECT pt.tag.id, w.createdAt FROM Wishlist w "
            + "JOIN PlaceTag pt ON pt.place.id = w.place.id "
            + "WHERE w.user.id = :userId AND w.createdAt >= :since")
    List<Object[]> findTagsWithCreatedAt(@Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    @Query(value = """
            SELECT pt.tag_id, COUNT(DISTINCT w.user_id)
            FROM wishlists w
            JOIN place_tags pt ON pt.place_id = w.place_id
            JOIN users u ON u.id = w.user_id
            WHERE u.gender = :gender AND u.age_group = :ageGroup
              AND w.created_at >= :since
            GROUP BY pt.tag_id
            ORDER BY COUNT(DISTINCT w.user_id) DESC
            LIMIT :tagLimit
            """, nativeQuery = true)
    List<Object[]> findGroupTagPopularity(
            @Param("gender") String gender,
            @Param("ageGroup") String ageGroup,
            @Param("since") LocalDateTime since,
            @Param("tagLimit") int tagLimit);
}
