package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceFilterResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceMapItemResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
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
public class PlaceSearchService {

    private static final int DEFAULT_SIZE = 10;

    private final PlaceRepository placeRepository;
    private final WishlistReader wishlistReader;
    private final FileStorage fileStorage;

    public PlaceFilterResponse getFilteredPlaces(Long userId, PlaceFilterRequest request, Double longitude, Double latitude) {
        int size = resolveSize(request.size());
        List<Long> placeIds = placeRepository.findByFilter(request, longitude, latitude, size);
        boolean hasNext = hasNextPage(placeIds, size);
        placeIds = limitToPageSize(placeIds, size, hasNext);

        if (placeIds.isEmpty()) {
            return PlaceFilterResponse.empty();
        }

        List<PlaceMapItemResponse> responses = buildPlaceResponses(userId, placeIds);
        Long lastPlaceId = placeIds.getLast();

        return new PlaceFilterResponse(responses, lastPlaceId, hasNext);
    }

    private int resolveSize(Integer requestedSize) {
        if (requestedSize != null) {
            return requestedSize;
        }
        return DEFAULT_SIZE;
    }

    private Map<Long, Place> toPlaceMap(List<Place> places) {
        return places.stream()
                .collect(Collectors.toMap(Place::getId, place -> place));
    }

    private List<PlaceMapItemResponse> toResponses(
            List<Long> placeIds,
            Map<Long, Place> placeMap,
            Set<Long> wishedPlaceIds
    ) {
        return placeIds.stream()
                .map(placeMap::get)
                .filter(Objects::nonNull)
                .map(place -> PlaceMapItemResponse.of(
                        place,
                        place.getPlaceDetail(),
                        wishedPlaceIds.contains(place.getId()),
                        fileStorage
                ))
                .toList();
    }

    public PlaceFilterResponse getNearbyFilteredPlaces(
            Long userId,
            PlaceFilterRequest request,
            double longitude,
            double latitude,
            double radiusMeters
    ) {
        int size = resolveSize(request.size());
        List<Long> placeIds = fetchNearbyPlaceIds(request, longitude, latitude, radiusMeters, size);
        boolean hasNext = hasNextPage(placeIds, size);
        placeIds = limitToPageSize(placeIds, size, hasNext);

        if (placeIds.isEmpty()) {
            return PlaceFilterResponse.empty();
        }

        List<PlaceMapItemResponse> responses = buildPlaceResponses(userId, placeIds);
        Long lastPlaceId = placeIds.getLast();

        return new PlaceFilterResponse(responses, lastPlaceId, hasNext);
    }

    private List<Long> fetchNearbyPlaceIds(
            PlaceFilterRequest request,
            double longitude,
            double latitude,
            double radiusMeters,
            int size
    ) {
        return placeRepository.findNearbyPlacesWithFilters(request, longitude, latitude, radiusMeters, size);
    }

    private boolean hasNextPage(List<Long> placeIds, int size) {
        return placeIds.size() > size;
    }

    private List<Long> limitToPageSize(List<Long> placeIds, int size, boolean hasNext) {
        return hasNext ? placeIds.subList(0, size) : placeIds;
    }

    private List<PlaceMapItemResponse> buildPlaceResponses(Long userId, List<Long> placeIds) {
        List<Place> places = placeRepository.findAllByIdWithDetails(placeIds);
        Map<Long, Place> placeMap = toPlaceMap(places);
        Set<Long> wishedPlaceIds = wishlistReader.getWishedPlaceIdSet(userId, placeIds);
        return toResponses(placeIds, placeMap, wishedPlaceIds);
    }

}
