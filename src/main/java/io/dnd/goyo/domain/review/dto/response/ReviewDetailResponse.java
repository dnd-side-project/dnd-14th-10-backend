package io.dnd.goyo.domain.review.dto.response;

import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.entity.ReviewImage;
import io.dnd.goyo.domain.review.entity.ReviewTag;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailResponse(
        Long reviewId,
        Long placeId,
        Long userId,
        String userNickname,
        String userProfileImg,
        Double rating,
        Mood mood,
        SpaceSize spaceSize,
        OutletScore outletScore,
        CrowdStatus crowdStatus,
        String content,
        List<ReviewTagResponse> tags,
        List<ReviewImageResponse> images,
        LocalDateTime visitedAt,
        LocalDateTime createdAt
) {

    public static ReviewDetailResponse of(Review review, List<ReviewTag> reviewTags, List<ReviewImage> reviewImages) {
        List<ReviewTagResponse> tags = reviewTags.stream()
                .map(ReviewTagResponse::from)
                .toList();

        List<ReviewImageResponse> images = reviewImages.stream()
                .map(ReviewImageResponse::from)
                .toList();

        return new ReviewDetailResponse(
                review.getId(),
                review.getPlace().getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getUser().getProfileImg(),
                review.getRating(),
                review.getMood(),
                review.getSpaceSize(),
                review.getOutletScore(),
                review.getCrowdStatus(),
                review.getContent(),
                tags,
                images,
                review.getVisitedAt(),
                review.getCreatedAt()
        );
    }
}
