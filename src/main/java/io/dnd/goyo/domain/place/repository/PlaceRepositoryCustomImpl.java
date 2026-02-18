package io.dnd.goyo.domain.place.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Long> findByThemeScore(
            double longitude,
            double latitude,
            double radiusMeters,
            String category,
            String scoreColumn,
            double minScore,
            double maxScore,
            int limit
    ) {
        String sql = String.format("""
                SELECT p.id FROM places p
                JOIN place_details pd ON pd.place_id = p.id
                WHERE ST_DWithin(
                    CAST(p.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                    :radiusMeters
                )
                AND p.category = :category
                AND p.status = 'ACTIVE'
                AND (pd.%s::float / (pd.review_count + 1)) >= :minScore
                AND (pd.%s::float / (pd.review_count + 1)) <= :maxScore
                ORDER BY (pd.wish_count + pd.review_count) DESC,
                    ST_Distance(
                        CAST(p.location AS geography),
                        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                    ) ASC
                LIMIT :limit
                """, scoreColumn, scoreColumn);

        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter("longitude", longitude)
                .setParameter("latitude", latitude)
                .setParameter("radiusMeters", radiusMeters)
                .setParameter("category", category)
                .setParameter("minScore", minScore)
                .setParameter("maxScore", maxScore)
                .setParameter("limit", limit)
                .getResultList();

        return result.stream()
                .map(id -> ((Number) id).longValue())
                .toList();
    }
}