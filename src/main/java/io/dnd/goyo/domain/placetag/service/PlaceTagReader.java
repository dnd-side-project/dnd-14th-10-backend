package io.dnd.goyo.domain.placetag.service;

import io.dnd.goyo.domain.placetag.repository.PlaceTagRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceTagReader {

    private final PlaceTagRepository placeTagRepository;

    private static final int CANDIDATE_LIMIT = 20;

    public List<Long> findCandidatePlaceIds(
            List<Long> tagIds,
            long regionCode,
            String category,
            List<Long> excludeIds
    ) {
        if (tagIds.isEmpty()) {
            return List.of();
        }

        List<Long> safeExcludeIds = toSafeExcludeIds(excludeIds);
        return placeTagRepository.findCandidatePlaceIds(
                tagIds, regionCode, category, safeExcludeIds, CANDIDATE_LIMIT);
    }

    public Map<Long, List<Long>> getPlaceTagMappings(List<Long> placeIds) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<Long>> result = new HashMap<>();

        for (Object[] row : placeTagRepository.findPlaceTagMappings(placeIds)) {
            Long placeId = (Long) row[0];
            Long tagId = (Long) row[1];
            if (!result.containsKey(placeId)) {
                result.put(placeId, new ArrayList<>());
            }
            result.get(placeId).add(tagId);
        }
        return result;
    }

    private List<Long> toSafeExcludeIds(List<Long> excludeIds) {
        if (excludeIds.isEmpty()) {
            return List.of(0L);
        }
        return excludeIds;
    }
}
