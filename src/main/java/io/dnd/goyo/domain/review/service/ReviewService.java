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
import io.dnd.goyo.domain.review.dto.request.ReviewImageRequest;
import io.dnd.goyo.domain.review.dto.request.ReviewUpdateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewCreateResponse;
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
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.review.enums.ReviewSortType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReviewCreateResponse createReview(Long userId, ReviewCreateRequest request) {
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

        PlaceDetail placeDetail = getPlaceDetailForUpdate(place.getId());
        placeDetail.addReviewScores(ReviewScores.from(
                request.rating().doubleValue(), request.outletScore(),
                request.crowdStatus(), request.spaceSize(), request.mood()));

        long reviewOrder = placeDetail.getReviewCount();

        boolean hasImages = request.images() != null && !request.images().isEmpty();
        eventPublisher.publishEvent(new ActivityEvent(userId, ActivityType.REVIEW, 1, hasImages ? 1 : 0));

        String representativeImageUrl = null;
        if (request.images() != null) {
            representativeImageUrl = request.images().stream()
                    .filter(ReviewImageRequest::isPrimary)
                    .findFirst()
                    .map(img -> fileStorage.generatePublicUrl(img.imageKey()))
                    .orElse(null);
        }

        return ReviewCreateResponse.of(review.getId(), representativeImageUrl, reviewOrder);
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

    public Page<ReviewDetailResponse> getMyReviews(Long userId, Pageable pageable, ReviewSortType sortType) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortType.toSort());
        Page<Review> reviewPage = reviewRepository.findAllByUserIdAndStatus(userId, ReviewStatus.ACTIVE, sorted);

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

        PlaceDetail placeDetail = getPlaceDetailForUpdate(review.getPlace().getId());
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

        List<ReviewImage> existingImages = reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId);
        Set<String> oldKeys = existingImages.stream()
                .map(ReviewImage::getImageKey)
                .collect(Collectors.toSet());

        Set<String> newKeys = (request.images() != null)
                ? request.images().stream().map(img -> img.imageKey()).collect(Collectors.toSet())
                : Set.of();

        List<String> orphanedKeys = oldKeys.stream()
                .filter(key -> !newKeys.contains(key))
                .toList();

        reviewImageRepository.deleteAllByReviewId(reviewId);
        if (request.images() != null && !request.images().isEmpty()) {
            List<ReviewImage> images = request.images().stream()
                    .map(img -> ReviewImage.of(review, img.imageKey(), img.sequence(), img.isPrimary()))
                    .toList();
            reviewImageRepository.saveAll(images);
        }

        if (!orphanedKeys.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        fileStorage.deleteObjects(orphanedKeys);
                    } catch (Exception e) {
                        log.warn("리뷰 이미지 MinIO 삭제 실패 (reviewId: {}): {}", reviewId, e.getMessage());
                    }
                }
            });
        }

        reviewTagService.replaceReviewTags(review, request.tagIds());

        placeDetail.addReviewScores(ReviewScores.from(
                request.rating().doubleValue(), request.outletScore(),
                request.crowdStatus(), request.spaceSize(), request.mood()));

        boolean hadImages = !existingImages.isEmpty();
        boolean hasImages = request.images() != null && !request.images().isEmpty();
        int imageDelta = 0;
        if (!hadImages && hasImages) imageDelta = 1;
        if (hadImages && !hasImages) imageDelta = -1;
        if (imageDelta != 0) {
            eventPublisher.publishEvent(new ActivityEvent(userId, ActivityType.IMAGE, 0, imageDelta));
        }
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = getActiveReview(reviewId);
        validateOwner(userId, review);

        boolean hadImages = !reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId).isEmpty();

        PlaceDetail placeDetail = getPlaceDetailForUpdate(review.getPlace().getId());
        placeDetail.removeReviewScores(ReviewScores.from(review));

        review.delete();

        eventPublisher.publishEvent(new ActivityEvent(userId, ActivityType.REVIEW, -1, hadImages ? -1 : 0));
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

    private PlaceDetail getPlaceDetailForUpdate(Long placeId) {
        return placeDetailRepository.findByPlaceIdForUpdate(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
    }

    private void validateOwner(Long userId, Review review) {
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_OWNER);
        }
    }
}
