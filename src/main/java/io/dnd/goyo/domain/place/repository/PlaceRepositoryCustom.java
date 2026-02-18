package io.dnd.goyo.domain.place.repository;

import java.util.List;

public interface PlaceRepositoryCustom {

    List<Long> findByThemeScore(
            double longitude,
            double latitude,
            double radiusMeters,
            String category,
            String scoreColumn,
            double minScore,
            double maxScore,
            int limit
    );
}