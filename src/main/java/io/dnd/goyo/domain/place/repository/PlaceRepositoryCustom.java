package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import java.util.List;

public interface PlaceRepositoryCustom {

    List<Long> findByFilter(PlaceFilterRequest request, int size);

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