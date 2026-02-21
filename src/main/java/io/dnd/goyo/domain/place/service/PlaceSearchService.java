package io.dnd.goyo.domain.place.service;

import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.dto.PlaceWithDistance;
import io.dnd.goyo.domain.place.dto.request.NearbyFilterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceFilterRequest;
import io.dnd.goyo.domain.place.dto.response.PlaceFilterResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceMapItemResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceSearchService {

    private final PlaceRepository placeRepository;
    private final FileStorage fileStorage;

    public PlaceFilterResponse getFilteredPlaces(PlaceFilterRequest request, double longitude, double latitude) {
        int size = request.resolvedSize();
        List<PlaceWithDistance> placesWithDistance = placeRepository.findByFilterWithDistance(request, longitude, latitude, size);
        boolean hasNext = placesWithDistance.size() > size;
        if (hasNext) {
            placesWithDistance = placesWithDistance.subList(0, size);
        }

        if (placesWithDistance.isEmpty()) {
            return PlaceFilterResponse.empty();
        }

        List<Long> placeIds = placesWithDistance.stream()
                .map(PlaceWithDistance::placeId)
                .toList();
        List<PlaceMapItemResponse> responses = buildPlaceResponses(placeIds);

        Double lastDistance = placesWithDistance.getLast().distance();
        return new PlaceFilterResponse(responses, lastDistance, hasNext);
    }

    public PlaceFilterResponse getNearbyFilteredPlaces(NearbyFilterRequest request, double longitude, double latitude) {
        int size = request.resolvedSize();
        List<PlaceWithDistance> placesWithDistance = placeRepository.findNearbyWithFilters(request, longitude, latitude, size);
        boolean hasNext = placesWithDistance.size() > size;
        if (hasNext) {
            placesWithDistance = placesWithDistance.subList(0, size);
        }

        if (placesWithDistance.isEmpty()) {
            return PlaceFilterResponse.empty();
        }

        List<Long> placeIds = placesWithDistance.stream()
                .map(PlaceWithDistance::placeId)
                .toList();
        List<PlaceMapItemResponse> responses = buildPlaceResponses(placeIds);

        Double lastDistance = placesWithDistance.getLast().distance();
        return new PlaceFilterResponse(responses, lastDistance, hasNext);
    }

    private Map<Long, Place> toPlaceMap(List<Place> places) {
        return places.stream()
                .collect(Collectors.toMap(Place::getId, place -> place));
    }

    private List<PlaceMapItemResponse> toResponses(
            List<Long> placeIds,
            Map<Long, Place> placeMap
    ) {
        return placeIds.stream()
                .map(placeMap::get)
                .filter(Objects::nonNull)
                .map(place -> PlaceMapItemResponse.of(
                        place,
                        place.getPlaceDetail(),
                        fileStorage
                ))
                .toList();
    }

    private List<PlaceMapItemResponse> buildPlaceResponses(List<Long> placeIds) {
        List<Place> places = placeRepository.findAllByIdWithDetails(placeIds);
        Map<Long, Place> placeMap = toPlaceMap(places);
        return toResponses(placeIds, placeMap);
    }

}
