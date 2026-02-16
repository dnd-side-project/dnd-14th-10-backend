package io.dnd.goyo.domain.wishlist.service;

import io.dnd.goyo.common.util.TimeDecayUtils;
import io.dnd.goyo.domain.user.enums.AgeGroup;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.wishlist.dto.TagPopularityProjection;
import io.dnd.goyo.domain.wishlist.dto.TagWithCreatedAtDto;
import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistReader {

    private static final int GROUP_TAG_LIMIT = 20;

    private final WishlistRepository wishlistRepository;

    public List<Long> getWishedPlaceIds(Long userId, List<Long> placeIds) {
        if (placeIds.isEmpty()) {
            return List.of();
        }

        return wishlistRepository.findPlaceIdsByUserIdAndPlaceIds(userId, placeIds);
    }

    public List<Long> getAllWishedPlaceIds(Long userId, LocalDateTime since) {
        return wishlistRepository.findPlaceIdsByUserId(userId, since);
    }

    public Map<Long, Double> getTagWeights(Long userId, LocalDateTime since, LocalDateTime now) {
        Map<Long, Double> tagWeights = new HashMap<>();
        for (TagWithCreatedAtDto dto : wishlistRepository.findTagsWithCreatedAt(userId, since)) {
            double weight = TimeDecayUtils.calculateWeight(dto.createdAt(), now);
            tagWeights.merge(dto.tagId(), weight, Double::sum);
        }
        return tagWeights;
    }

    public Map<Long, Double> getGroupTagPopularity(Gender gender, AgeGroup ageGroup, LocalDateTime since) {
        List<TagPopularityProjection> results = wishlistRepository.findGroupTagPopularity(
                gender.name(), ageGroup.name(), since, GROUP_TAG_LIMIT);

        if (results.isEmpty()) {
            return Map.of();
        }

        double maxPopularity = results.stream()
                .mapToDouble(projection -> projection.getPopularity().doubleValue())
                .max()
                .orElse(1.0);

        Map<Long, Double> normalized = new HashMap<>();
        for (TagPopularityProjection projection : results) {
            normalized.put(projection.getTagId(), projection.getPopularity().doubleValue() / maxPopularity);
        }
        return normalized;
    }
}
