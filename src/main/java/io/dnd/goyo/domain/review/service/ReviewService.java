package io.dnd.goyo.domain.review.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.ReviewScores;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.service.PlaceReader;
import io.dnd.goyo.domain.review.dto.request.ReviewCreateRequest;
import io.dnd.goyo.domain.review.dto.request.ReviewUpdateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewDetailResponse;
import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.entity.ReviewImage;
import io.dnd.goyo.domain.review.entity.ReviewTag;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.review.repository.ReviewImageRepository;
import io.dnd.goyo.domain.review.repository.ReviewRepository;
import io.dnd.goyo.domain.review.repository.ReviewTagRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final PlaceDetailRepository placeDetailRepository;
    private final UserReader userReader;
    private final PlaceReader placeReader;
    private final ReviewTagService reviewTagService;
    private final FileStorage fileStorage;

    @Transactional
    public Long createReview(Long userId, ReviewCreateRequest request) {
        User user = userReader.getUser(userId);
        Place place = placeReader.getPlace(request.placeId());

        Review review = Review.create(
                user, place, request.rating().doubleValue(), request.mood(),
                request.outletScore(), request.crowdStatus(), request.spaceSize(),
                request.content(), request.visitedAt());
        reviewRepository.save(review);

        if (request.images() != null && !request.images().isEmpty()) {
            List<ReviewImage> images = request.images().stream()
                    .map(img -> ReviewImage.of(review, img.imageKey(), img.sequence(), img.isPrimary()))
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        reviewTagService.registerReviewTags(review, request.tagIds());

        PlaceDetail placeDetail = getPlaceDetail(place.getId());
        placeDetail.addReviewScores(ReviewScores.from(
                request.rating().doubleValue(), request.outletScore(),
                request.crowdStatus(), request.spaceSize(), request.mood()));

        return review.getId();
    }

    public ReviewDetailResponse getReview(Long reviewId) {
        Review review = getActiveReviewWithUser(reviewId);
        List<ReviewTag> reviewTags = reviewTagRepository.findAllByReviewId(reviewId);
        List<ReviewImage> reviewImages = reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId);
        return ReviewDetailResponse.of(review, reviewTags, reviewImages, fileStorage);
    }

    public Page<ReviewDetailResponse> getReviewsByPlace(Long placeId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAllByPlaceIdAndStatus(placeId, ReviewStatus.ACTIVE, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(Review::getId)
                .toList();

        Map<Long, List<ReviewTag>> tagsByReviewId = reviewTagRepository.findAllByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(rt -> rt.getReview().getId()));

        Map<Long, List<ReviewImage>> imagesByReviewId = reviewImageRepository.findAllByReviewIdInOrderBySequence(reviewIds).stream()
                .collect(Collectors.groupingBy(ri -> ri.getReview().getId()));

        return reviewPage.map(review -> ReviewDetailResponse.of(
                review,
                tagsByReviewId.getOrDefault(review.getId(), List.of()),
                imagesByReviewId.getOrDefault(review.getId(), List.of()),
                fileStorage
        ));
    }

    public Page<ReviewDetailResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findAllByUserIdAndStatus(userId, ReviewStatus.ACTIVE, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
                .map(Review::getId)
                .toList();

        Map<Long, List<ReviewTag>> tagsByReviewId = reviewTagRepository.findAllByReviewIdIn(reviewIds).stream()
                .collect(Collectors.groupingBy(rt -> rt.getReview().getId()));

        Map<Long, List<ReviewImage>> imagesByReviewId = reviewImageRepository.findAllByReviewIdInOrderBySequence(reviewIds).stream()
                .collect(Collectors.groupingBy(ri -> ri.getReview().getId()));

        return reviewPage.map(review -> ReviewDetailResponse.of(
                review,
                tagsByReviewId.getOrDefault(review.getId(), List.of()),
                imagesByReviewId.getOrDefault(review.getId(), List.of()),
                fileStorage
        ));
    }

    @Transactional
    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = getActiveReview(reviewId);
        validateOwner(userId, review);

        PlaceDetail placeDetail = getPlaceDetail(review.getPlace().getId());
        placeDetail.removeReviewScores(ReviewScores.from(review));

        review.update(
                request.rating().doubleValue(),
                request.mood(),
                request.outletScore(),
                request.crowdStatus(),
                request.spaceSize(),
                request.content(),
                request.visitedAt()
        );

        reviewImageRepository.deleteAllByReviewId(reviewId);
        if (request.images() != null && !request.images().isEmpty()) {
            List<ReviewImage> images = request.images().stream()
                    .map(img -> ReviewImage.of(review, img.imageKey(), img.sequence(), img.isPrimary()))
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        reviewTagService.replaceReviewTags(review, request.tagIds());

        placeDetail.addReviewScores(ReviewScores.from(
                request.rating().doubleValue(), request.outletScore(),
                request.crowdStatus(), request.spaceSize(), request.mood()));
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = getActiveReview(reviewId);
        validateOwner(userId, review);

        PlaceDetail placeDetail = getPlaceDetail(review.getPlace().getId());
        placeDetail.removeReviewScores(ReviewScores.from(review));

        review.delete();
    }

    private Review getActiveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (review.getStatus() != ReviewStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    private Review getActiveReviewWithUser(Long reviewId) {
        Review review = reviewRepository.findByIdWithUserAndPlace(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        if (review.getStatus() != ReviewStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return review;
    }

    private PlaceDetail getPlaceDetail(Long placeId) {
        return placeDetailRepository.findByPlaceId(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    private void validateOwner(Long userId, Review review) {
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_OWNER);
        }
    }
}
