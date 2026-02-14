package io.dnd.goyo.domain.wishlist.service;

import io.dnd.goyo.common.util.TimeDecayUtils;
import io.dnd.goyo.domain.user.enums.AgeGroup;
import io.dnd.goyo.domain.user.enums.Gender;
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
        return wishlistRepository.findPlaceIdsByUserIdAndPlaceIds(userId, placeIds);
    }

    public List<Long> getAllWishedPlaceIds(Long userId, LocalDateTime since) {
        return wishlistRepository.findPlaceIdsByUserId(userId, since);
    }

    public Map<Long, Double> getTagWeights(Long userId, LocalDateTime since, LocalDateTime now) {
        Map<Long, Double> tagWeights = new HashMap<>();
        for (Object[] row : wishlistRepository.findTagsWithCreatedAt(userId, since)) {
            Long tagId = (Long) row[0];
            LocalDateTime createdAt = (LocalDateTime) row[1];
            double weight = TimeDecayUtils.calculateWeight(createdAt, now);
            tagWeights.merge(tagId, weight, Double::sum);
        }
        return tagWeights;
    }

    public Map<Long, Double> getGroupTagPopularity(Gender gender, AgeGroup ageGroup, LocalDateTime since) {
        List<Object[]> rows = wishlistRepository.findGroupTagPopularity(
                gender.name(), ageGroup.name(), since, GROUP_TAG_LIMIT);
        
        if (rows.isEmpty()) {
            return Map.of();
        }

        double maxPopularity = rows.stream()
                .mapToDouble(row -> ((Number) row[1]).doubleValue())
                .max()
                .orElse(1.0);

        Map<Long, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            Long tagId = ((Number) row[0]).longValue();
            double count = ((Number) row[1]).doubleValue();
            result.put(tagId, count / maxPopularity);
        }
        return result;
    }
}
