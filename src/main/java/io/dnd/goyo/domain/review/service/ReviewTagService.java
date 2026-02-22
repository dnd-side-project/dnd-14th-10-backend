package io.dnd.goyo.domain.review.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.entity.ReviewTag;
import io.dnd.goyo.domain.review.entity.ReviewTags;
import io.dnd.goyo.domain.review.repository.ReviewTagRepository;
import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewTagService {

    private final ReviewTagRepository reviewTagRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void registerReviewTags(Review review, List<Long> tagIds) {
        ReviewTags reviewTags = ReviewTags.from(tagIds);

        List<Tag> tags = findAndValidateTags(reviewTags.tagIds());

        List<ReviewTag> reviewTagList = tags.stream()
                .map(tag -> ReviewTag.of(review, tag))
                .toList();

        reviewTagRepository.saveAll(reviewTagList);
    }

    @Transactional
    public void replaceReviewTags(Review review, List<Long> tagIds) {
        reviewTagRepository.deleteAllByReviewId(review.getId());
        registerReviewTags(review, tagIds);
    }

    private List<Tag> findAndValidateTags(List<Long> tagIds) {
        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "존재하지 않는 태그가 포함되어 있습니다.");
        }
        return tags;
    }
}
