package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceRecommendationService {

    private static final double DEFAULT_RADIUS_METERS = 3000.0;
    private static final int RECENT_DAYS = 30;
    private static final int LIMIT = 6;
    private static final int BAYESIAN_MIN_REVIEWS = 2;
    private static final double BAYESIAN_PRIOR_RATING = 3.5;

    private final PlaceRepository placeRepository;
    private final WishlistRepository wishlistRepository;

    public List<PlaceSummaryResponse> getNewPlaces(
            Long userId,
            double longitude,
            double latitude,
            int regionCode,
            PlaceCategory category,
            Integer radiusMeters
    ) {
        List<Long> placeIds = findNewPlaceIds(longitude, latitude, regionCode, category, radiusMeters);

        if (placeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Place> placeMap = findPlacesMap(placeIds);
        Set<Long> wishedPlaceIds = findWishedPlaceIds(userId, placeIds);

        return createPlaceSummaries(placeIds, placeMap, wishedPlaceIds);
    }

    public List<PlaceSummaryResponse> getPopularPlaces(
            Long userId,
            double longitude,
            double latitude,
            PlaceCategory category,
            Integer radiusMeters
    ) {
        List<Long> placeIds = findPopularPlaceIds(longitude, latitude, category, radiusMeters);

        if (placeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Place> placeMap = findPlacesMap(placeIds);
        Set<Long> wishedPlaceIds = findWishedPlaceIds(userId, placeIds);

        return createPlaceSummaries(placeIds, placeMap, wishedPlaceIds);
    }

    private List<Long> findPopularPlaceIds(
            double longitude,
            double latitude,
            PlaceCategory category,
            Integer radiusMeters
    ) {
        double radius = DEFAULT_RADIUS_METERS;
        if (radiusMeters != null) {
            radius = radiusMeters;
        }
        return placeRepository.findPopularPlaceIds(
                longitude, latitude, radius, category.name(),
                BAYESIAN_MIN_REVIEWS, BAYESIAN_PRIOR_RATING, LIMIT
        );
    }

    private List<Long> findNewPlaceIds(
            double longitude,
            double latitude,
            int regionCode,
            PlaceCategory category,
            Integer radiusMeters
    ) {
        double radius = DEFAULT_RADIUS_METERS;
        if (radiusMeters != null) {
            radius = radiusMeters;
        }
        return placeRepository.findNewPlaceIdsByRegionCode(
                longitude, latitude, radius, regionCode, category.name(), RECENT_DAYS, LIMIT
        );
    }

    private Map<Long, Place> findPlacesMap(List<Long> placeIds) {
        return placeRepository.findAllByIdWithDetails(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, Function.identity()));
    }

    private List<PlaceSummaryResponse> createPlaceSummaries(
            List<Long> sortedIds,
            Map<Long, Place> placeMap,
            Set<Long> wishedPlaceIds
    ) {
        return sortedIds.stream()
                .map(placeMap::get)
                .filter(Objects::nonNull)
                .map(place -> buildPlaceSummary(place, wishedPlaceIds))
                .toList();
    }

    private Set<Long> findWishedPlaceIds(Long userId, List<Long> placeIds) {
        return new HashSet<>(wishlistRepository.findPlaceIdsByUserIdAndPlaceIds(userId, placeIds));
    }

    private PlaceSummaryResponse buildPlaceSummary(Place place, Set<Long> wishedPlaceIds) {
        return PlaceSummaryResponse.of(
                place,
                place.getPlaceDetail(),
                getRepresentativeImageUrl(place),
                wishedPlaceIds.contains(place.getId())
        );
    }

    private String getRepresentativeImageUrl(Place place) {
        return place.getImages().stream()
                .filter(PlaceImage::isRepresentativeFlag)
                .findFirst()
                .map(PlaceImage::getImageKey)
                .orElse(null);
    }
}
