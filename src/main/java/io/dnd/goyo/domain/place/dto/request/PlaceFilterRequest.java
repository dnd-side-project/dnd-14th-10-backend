package io.dnd.goyo.domain.place.dto.request;

import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public record PlaceFilterRequest(
        PlaceCategory category,
        SpaceSize spaceSize,
        List<Mood> moods,
        List<@Min(10000) @Max(99999) Long> regionCodes,
        Long lastPlaceId,
        @Min(1) @Max(50) Integer size
) {}
