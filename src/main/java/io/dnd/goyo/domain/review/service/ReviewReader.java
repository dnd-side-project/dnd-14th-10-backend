package io.dnd.goyo.domain.review.service;

import io.dnd.goyo.common.util.TimeDecayUtils;
import io.dnd.goyo.domain.review.dto.TagWithCreatedAtDto;
import io.dnd.goyo.domain.review.repository.ReviewRepository;
import io.dnd.goyo.domain.review.repository.ReviewTagRepository;
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
public class ReviewReader {

    private final ReviewRepository reviewRepository;
    private final ReviewTagRepository reviewTagRepository;

    public List<Long> getReviewedPlaceIds(Long userId, LocalDateTime since) {
        return reviewRepository.findPlaceIdsByUserIdAndStatus(userId, since);
    }

    public Map<Long, Double> getReviewTagWeights(Long userId, LocalDateTime since, LocalDateTime now) {
        Map<Long, Double> tagWeights = new HashMap<>();
        for (TagWithCreatedAtDto dto : reviewTagRepository.findTagsWithCreatedAt(userId, since)) {
            double weight = TimeDecayUtils.calculateWeight(dto.createdAt(), now);
            tagWeights.merge(dto.tagId(), weight, Double::sum);
        }
        return tagWeights;
    }
}
