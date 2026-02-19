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

    public PlaceFilterResponse getFilteredPlaces(Long userId, PlaceFilterRequest request) {
        int size = resolveSize(request.size());

        List<Long> placeIds = placeRepository.findByFilter(request, size);

        boolean hasNext = placeIds.size() > size;
        if (hasNext) {
            placeIds = placeIds.subList(0, size);
        }

        if (placeIds.isEmpty()) {
            return new PlaceFilterResponse(List.of(), null, false);
        }

        List<Place> places = placeRepository.findAllByIdWithDetails(placeIds);
        Map<Long, Place> placeMap = toPlaceMap(places);

        Set<Long> wishedPlaceIds = findWishedPlaceIds(userId, placeIds);

        List<PlaceMapItemResponse> responses = toResponses(placeIds, placeMap, wishedPlaceIds);

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

    private Set<Long> findWishedPlaceIds(Long userId, List<Long> placeIds) {
        if (userId == null) {
            return Set.of();
        }
        return new HashSet<>(wishlistReader.getWishedPlaceIds(userId, placeIds));
    }
}
