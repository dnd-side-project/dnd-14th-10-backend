package io.dnd.goyo.domain.place.dto.response;

import java.util.List;

public record ThemeRecommendationResponse(
        String themeType,
        String themeValue,
        List<PlaceSummaryResponse> places
) {
}