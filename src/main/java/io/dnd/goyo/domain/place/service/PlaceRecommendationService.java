package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.response.PlaceSummaryResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.RegionCode;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagReader;
import io.dnd.goyo.domain.review.service.ReviewReader;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private static final double USER_TAG_WEIGHT = 0.6;
    private static final double GROUP_TAG_WEIGHT = 0.4;
    private static final double DISTANCE_DECAY_METERS = 3000.0;

    private final PlaceRepository placeRepository;
    private final WishlistReader wishlistReader;
    private final ReviewReader reviewReader;
    private final PlaceTagReader placeTagReader;
    private final UserReader userReader;
    private final GeometryUtils geometryUtils;

    public List<PlaceSummaryResponse> getNewPlaces(
            Long userId,
            double longitude,
            double latitude,
            long regionCode,
            PlaceCategory category,
            Integer radiusMeters
    ) {
        long siGunGuCode = new RegionCode(regionCode).getSiGunGuCode();
        List<Long> placeIds = findNewPlaceIds(longitude, latitude, siGunGuCode, category, radiusMeters);

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

    public List<PlaceSummaryResponse> getSimilarPlaces(
            Long userId,
            long regionCode,
            PlaceCategory category,
            double longitude,
            double latitude
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(RECENT_DAYS);

        Map<Long, Double> userTagWeights = buildUserTagWeights(userId, since, now);
        if (userTagWeights.isEmpty()) {
            return List.of();
        }

        User user = userReader.getUser(userId);
        Map<Long, Double> groupTagNorm = buildGroupTagNorm(user, since);

        long siGunGuCode = new RegionCode(regionCode).getSiGunGuCode();
        List<Long> candidatePlaceIds = findCandidatePlaceIds(
                userId, since, userTagWeights, groupTagNorm, siGunGuCode, category);

        if (candidatePlaceIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Place> candidatePlaceMap = findPlacesMap(candidatePlaceIds);
        List<Long> scoredPlaceIds = scoreAndRank(
                candidatePlaceIds,
                candidatePlaceMap,
                userTagWeights,
                groupTagNorm,
                longitude,
                latitude
        );

        Set<Long> wishedSet = new HashSet<>(wishlistReader.getAllWishedPlaceIds(userId, since));

        return createPlaceSummaries(scoredPlaceIds, candidatePlaceMap, wishedSet);
    }

    private List<Long> findCandidatePlaceIds(
            Long userId,
            LocalDateTime since,
            Map<Long, Double> userTagWeights,
            Map<Long, Double> groupTagNorm,
            long regionCode,
            PlaceCategory category
    ) {
        Set<Long> allTagIds = new HashSet<>(userTagWeights.keySet());
        allTagIds.addAll(groupTagNorm.keySet());

        List<Long> interactedPlaceIds = getWishedOrReviewedPlaceIds(userId, since);

        List<Long> searchTagIds = new ArrayList<>(allTagIds);
        return placeTagReader.findCandidatePlaceIds(
                searchTagIds, regionCode, category.name(), interactedPlaceIds);
    }

    private List<Long> scoreAndRank(
            List<Long> candidatePlaceIds,
            Map<Long, Place> placeMap,
            Map<Long, Double> userTagWeights,
            Map<Long, Double> groupTagNorm,
            double longitude,
            double latitude
    ) {
        Map<Long, List<Long>> placeTagMap = placeTagReader.getPlaceTagMappings(candidatePlaceIds);

        return candidatePlaceIds.stream()
                .map(placeMap::get)
                .filter(Objects::nonNull)
                .map(place -> {
                    double score = calculateScore(place, placeTagMap, userTagWeights, groupTagNorm, longitude, latitude);
                    return Map.entry(place.getId(), score);
                })
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(LIMIT)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Map<Long, Double> buildUserTagWeights(Long userId, LocalDateTime since, LocalDateTime now) {
        Map<Long, Double> wishlistWeights = wishlistReader.getTagWeights(userId, since, now);
        Map<Long, Double> reviewWeights = reviewReader.getReviewTagWeights(userId, since, now);

        Map<Long, Double> merged = new HashMap<>(wishlistWeights);
        reviewWeights.forEach((tagId, weight)
                -> merged.merge(tagId, weight, Double::sum));

        return merged;
    }

    private Map<Long, Double> buildGroupTagNorm(User user, LocalDateTime since) {
        if (user.getGender() == null || user.getAgeGroup() == null) {
            return Map.of();
        }
        return wishlistReader.getGroupTagPopularity(user.getGender(), user.getAgeGroup(), since);
    }

    private List<Long> getWishedOrReviewedPlaceIds(Long userId, LocalDateTime since) {
        Set<Long> interactedIds = new HashSet<>();
        interactedIds.addAll(wishlistReader.getAllWishedPlaceIds(userId, since));
        interactedIds.addAll(reviewReader.getReviewedPlaceIds(userId, since));

        return new ArrayList<>(interactedIds);
    }

    private double calculateScore(
            Place place,
            Map<Long, List<Long>> placeTagMap,
            Map<Long, Double> userTagWeights,
            Map<Long, Double> groupTagNorm,
            double userLng,
            double userLat
    ) {
        double distance = geometryUtils.calculateDistance(
                userLng,
                userLat,
                place.getLocation().getX(),
                place.getLocation().getY()
        );

        List<Long> tags = placeTagMap.getOrDefault(place.getId(), List.of());

        double userScore = 0.0;
        double groupScore = 0.0;
        for (Long tagId : tags) {
            userScore += userTagWeights.getOrDefault(tagId, 0.0);
            groupScore += groupTagNorm.getOrDefault(tagId, 0.0);
        }

        double distanceDecay = Math.exp(-distance / DISTANCE_DECAY_METERS);

        return (userScore * USER_TAG_WEIGHT + groupScore * GROUP_TAG_WEIGHT) * distanceDecay;
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
            long regionCode,
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
        return placeRepository.findAllByIdWithDetails(placeIds)
                .stream()
                .collect(Collectors.toMap(Place::getId, place -> place));
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
        return new HashSet<>(wishlistReader.getWishedPlaceIds(userId, placeIds));
    }

    private PlaceSummaryResponse buildPlaceSummary(Place place, Set<Long> wishedPlaceIds) {
        return PlaceSummaryResponse.of(
                place,
                place.getPlaceDetail(),
                place.getRepresentativeImageKey(),
                wishedPlaceIds.contains(place.getId())
        );
    }
}
