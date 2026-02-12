package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.Place;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("SELECT DISTINCT p FROM Place p " +
            "JOIN FETCH p.placeDetail " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id IN :ids")
    List<Place> findAllByIdWithDetails(@Param("ids") List<Long> ids);

    @Query(value = """
            SELECT p.id FROM places p
            WHERE ST_DWithin(
                CAST(p.location AS geography),
                CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                :radiusMeters
            )
            AND CAST(p.region_code AS TEXT) LIKE CAST(:regionCode AS TEXT) || '%'
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
            @Param("regionCode") int regionCode,
            @Param("category") String category,
            @Param("recentDays") int recentDays,
            @Param("limit") int limit
    );
}
