package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {

    @Query("SELECT DISTINCT p FROM Place p " +
            "JOIN FETCH p.placeDetail " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id IN :ids AND p.status = 'ACTIVE'")
    List<Place> findAllByIdWithDetails(@Param("ids") List<Long> ids);

    @Query("SELECT DISTINCT p FROM Place p " +
            "JOIN FETCH p.placeDetail " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id AND p.status = 'ACTIVE'")
    Optional<Place> findByIdWithDetails(@Param("id") Long id);

    @Query(value = """
            SELECT p.id FROM places p
            WHERE ST_DWithin(
                CAST(p.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND p.region_code BETWEEN :regionCode * 100000 AND (:regionCode * 100000) + 99999
            AND p.category = :category
            AND p.created_at >= CURRENT_TIMESTAMP - CAST(:recentDays || ' days' AS INTERVAL)
            AND p.status = 'ACTIVE'
            ORDER BY
                ST_Distance(
                    CAST(p.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                ) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findNewPlaceIdsByRegionCode(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("regionCode") long regionCode,
            @Param("category") String category,
            @Param("recentDays") int recentDays,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT p.id FROM places p
            JOIN place_details pd ON pd.place_id = p.id
            WHERE ST_DWithin(
                CAST(p.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND p.category = :category
            AND p.status = 'ACTIVE'
            AND pd.total_rating >= (pd.review_count * 3.0)
            ORDER BY (
                ((pd.total_rating + :minReviews * :priorRating) / (pd.review_count + :minReviews)) * 0.7
                + LN(pd.wish_count + 1) * 0.3
            ) DESC, p.created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findPopularPlaceIds(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("category") String category,
            @Param("minReviews") int minReviews,
            @Param("priorRating") double priorRating,
            @Param("limit") int limit
    );

    @Query(value = "SELECT p.id FROM Place p WHERE p.user.id = :userId AND p.status = :status",
           countQuery = "SELECT COUNT(p) FROM Place p WHERE p.user.id = :userId AND p.status = :status")
    Page<Long> findIdsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PlaceStatus status, Pageable pageable);

    @Query(value = "SELECT p.id FROM Place p JOIN p.placeDetail pd WHERE p.user.id = :userId AND p.status = 'ACTIVE' ORDER BY pd.wishCount DESC, p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Place p WHERE p.user.id = :userId AND p.status = 'ACTIVE'")
    Page<Long> findIdsByUserIdOrderByWishCountDesc(@Param("userId") Long userId, Pageable pageable);
}
