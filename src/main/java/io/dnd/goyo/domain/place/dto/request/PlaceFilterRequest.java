package io.dnd.goyo.domain.place.dto.request;

import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaceFilterRequest(
        PlaceCategory category,
        SpaceSize spaceSize,
        List<Mood> moods,
        List<@Min(10000) @Max(99999) Long> regionCodes,
        Double lastDistance,
        @Min(1) @Max(50) Integer size,
        @NotNull @Min(-180) @Max(180) Double longitude,
        @NotNull @Min(-90) @Max(90) Double latitude
) {
    private static final int DEFAULT_SIZE = 10;

    public int resolvedSize() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
