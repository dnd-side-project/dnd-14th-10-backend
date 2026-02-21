package io.dnd.goyo.domain.place.dto.request;

import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record NearbyFilterRequest(
        PlaceCategory category,
        SpaceSize spaceSize,
        List<Mood> moods,
        @Min(100) @Max(10000) Double radius,
        Double lastDistance,
        @Min(1) @Max(50) Integer size
) {
    private static final int DEFAULT_SIZE = 10;
    private static final double DEFAULT_RADIUS = 3000.0;

    public int resolvedSize() {
        return size != null ? size : DEFAULT_SIZE;
    }

    public double resolvedRadius() {
        return radius != null ? radius : DEFAULT_RADIUS;
    }
}