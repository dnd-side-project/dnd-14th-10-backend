package io.dnd.goyo.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.ReviewScores;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.service.PlaceReader;
import io.dnd.goyo.domain.review.dto.request.ReviewCreateRequest;
import io.dnd.goyo.domain.review.dto.request.ReviewImageRequest;
import io.dnd.goyo.domain.review.dto.request.ReviewUpdateRequest;
import io.dnd.goyo.domain.review.dto.response.ReviewDetailResponse;
import io.dnd.goyo.domain.review.entity.Review;
import io.dnd.goyo.domain.review.entity.ReviewImage;
import io.dnd.goyo.domain.review.enums.ReviewStatus;
import io.dnd.goyo.domain.review.repository.ReviewImageRepository;
import io.dnd.goyo.domain.review.repository.ReviewRepository;
import io.dnd.goyo.domain.review.repository.ReviewTagRepository;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private ReviewTagRepository reviewTagRepository;

    @Mock
    private PlaceDetailRepository placeDetailRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private PlaceReader placeReader;

    @Mock
    private ReviewTagService reviewTagService;

    @Mock
    private FileStorage fileStorage;

    private Review createReview(User user, Place place) {
        Review review = Review.create(user, place, 4.0, Mood.CALM,
                OutletScore.MANY, CrowdStatus.NORMAL, SpaceSize.MEDIUM,
                "좋은 카페입니다", null);
        ReflectionTestUtils.setField(review, "id", 1L);
        return review;
    }

    private ReviewCreateRequest createReviewCreateRequest(Long placeId) {
        return new ReviewCreateRequest(
                placeId, new BigDecimal("4.0"), List.of(1L, 2L), Mood.CALM, SpaceSize.MEDIUM,
                OutletScore.MANY, CrowdStatus.NORMAL, "좋은 카페입니다", null, null
        );
    }

    private ReviewUpdateRequest createReviewUpdateRequest() {
        return new ReviewUpdateRequest(
                new BigDecimal("5.0"), List.of(2L, 3L), Mood.SILENT, SpaceSize.LARGE,
                OutletScore.FEW, CrowdStatus.RELAX, "수정된 내용", null, null
        );
    }

    @Nested
    @DisplayName("리뷰 생성")
    class CreateReview {

        @Test
        void 리뷰_생성_성공() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(place.getId()).willReturn(placeId);
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            ReviewCreateRequest request = createReviewCreateRequest(placeId);

            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId)).willReturn(place);
            given(placeDetailRepository.findByPlaceId(placeId)).willReturn(Optional.of(placeDetail));

            // when
            reviewService.createReview(userId, request);

            // then
            verify(reviewRepository).save(any(Review.class));
            verify(reviewTagService).registerReviewTags(any(Review.class), eq(request.tagIds()));
            verify(placeDetail).addReviewScores(ReviewScores.from(
                    request.rating().doubleValue(), request.outletScore(),
                    request.crowdStatus(), request.spaceSize(), request.mood()));
        }

        @Test
        void 존재하지_않는_장소에_리뷰_작성_시_예외() {
            // given
            Long userId = 1L;
            Long placeId = 999L;
            User user = mock(User.class);
            ReviewCreateRequest request = createReviewCreateRequest(placeId);

            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId))
                    .willThrow(new BusinessException(ErrorCode.PLACE_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("리뷰 단건 조회")
    class GetReview {

        @Test
        void 리뷰_단건_조회_성공() {
            // given
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(1L);
            given(user.getNickname()).willReturn("테스터");
            given(user.getProfileImg()).willReturn("profile.jpg");
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);

            given(reviewRepository.findByIdWithUserAndPlace(reviewId)).willReturn(Optional.of(review));
            given(reviewTagRepository.findAllByReviewId(reviewId)).willReturn(List.of());
            given(reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId)).willReturn(List.of());

            // when
            ReviewDetailResponse response = reviewService.getReview(reviewId);

            // then
            assertThat(response.reviewId()).isEqualTo(reviewId);
            assertThat(response.rating()).isEqualTo(4.0);
        }

        @Test
        void 존재하지_않는_리뷰_조회_시_예외() {
            // given
            Long reviewId = 999L;
            given(reviewRepository.findByIdWithUserAndPlace(reviewId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(reviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }

        @Test
        void 삭제된_리뷰_조회_시_예외() {
            // given
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            Review review = createReview(user, place);
            review.delete();

            given(reviewRepository.findByIdWithUserAndPlace(reviewId)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.getReview(reviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 리뷰 목록 조회")
    class GetMyReviews {

        @Test
        void 내_리뷰_목록_조회_성공() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(userId);
            given(user.getNickname()).willReturn("테스터");
            given(user.getProfileImg()).willReturn("profile.jpg");
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(review), pageable, 1);

            given(reviewRepository.findAllByUserIdAndStatus(userId, ReviewStatus.ACTIVE, pageable))
                    .willReturn(reviewPage);
            given(reviewTagRepository.findAllByReviewIdIn(List.of(1L))).willReturn(List.of());
            given(reviewImageRepository.findAllByReviewIdInOrderBySequence(List.of(1L))).willReturn(List.of());

            // when
            Page<ReviewDetailResponse> result = reviewService.getMyReviews(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).reviewId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).rating()).isEqualTo(4.0);
        }

        @Test
        void 리뷰가_없으면_빈_페이지_반환() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Review> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(reviewRepository.findAllByUserIdAndStatus(userId, ReviewStatus.ACTIVE, pageable))
                    .willReturn(emptyPage);
            given(reviewTagRepository.findAllByReviewIdIn(List.of())).willReturn(List.of());
            given(reviewImageRepository.findAllByReviewIdInOrderBySequence(List.of())).willReturn(List.of());

            // when
            Page<ReviewDetailResponse> result = reviewService.getMyReviews(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("리뷰 수정")
    class UpdateReview {

        @Test
        void 리뷰_수정_성공() {
            // given
            Long userId = 1L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(userId);
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            ReviewUpdateRequest request = createReviewUpdateRequest();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(placeDetailRepository.findByPlaceId(10L)).willReturn(Optional.of(placeDetail));
            given(reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId)).willReturn(List.of());

            // when
            reviewService.updateReview(userId, reviewId, request);

            // then
            verify(placeDetail).removeReviewScores(new ReviewScores(4.0, OutletScore.MANY.getScore(),
                    CrowdStatus.NORMAL.getScore(), SpaceSize.MEDIUM.getScore(), Mood.CALM.getScore()));
            verify(placeDetail).addReviewScores(ReviewScores.from(request.rating().doubleValue(),
                    request.outletScore(), request.crowdStatus(),
                    request.spaceSize(), request.mood()));
            verify(reviewTagService).replaceReviewTags(eq(review), eq(request.tagIds()));
            assertThat(review.getRating()).isEqualTo(5.0);
        }

        @Test
        void 타인의_리뷰_수정_시_예외() {
            // given
            Long otherUserId = 2L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(1L);
            Review review = createReview(user, place);
            ReviewUpdateRequest request = createReviewUpdateRequest();

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(otherUserId, reviewId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_OWNER);
        }

        @Test
        void 이미지_교체_시_orphan_키에_대해_deleteObjects_호출() {
            // given
            Long userId = 1L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(userId);
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);
            PlaceDetail placeDetail = mock(PlaceDetail.class);

            ReviewImage oldImage1 = ReviewImage.of(review, "old-key-1", 0, true);
            ReviewImage oldImage2 = ReviewImage.of(review, "old-key-2", 1, false);

            ReviewUpdateRequest request = new ReviewUpdateRequest(
                    new BigDecimal("5.0"), List.of(2L, 3L), Mood.SILENT, SpaceSize.LARGE,
                    OutletScore.FEW, CrowdStatus.RELAX, "수정된 내용",
                    List.of(new ReviewImageRequest("old-key-1", 0, true),
                            new ReviewImageRequest("new-key-1", 1, false)),
                    null
            );

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(placeDetailRepository.findByPlaceId(10L)).willReturn(Optional.of(placeDetail));
            given(reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId))
                    .willReturn(List.of(oldImage1, oldImage2));

            // when
            reviewService.updateReview(userId, reviewId, request);

            // then — old-key-2는 새 요청에 없으므로 삭제되어야 함
            verify(fileStorage).deleteObjects(List.of("old-key-2"));
        }

        @Test
        void MinIO_삭제_실패해도_리뷰_수정_성공() {
            // given
            Long userId = 1L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(userId);
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);
            PlaceDetail placeDetail = mock(PlaceDetail.class);

            ReviewImage oldImage = ReviewImage.of(review, "old-key-1", 0, true);

            ReviewUpdateRequest request = new ReviewUpdateRequest(
                    new BigDecimal("5.0"), List.of(2L, 3L), Mood.SILENT, SpaceSize.LARGE,
                    OutletScore.FEW, CrowdStatus.RELAX, "수정된 내용", null, null
            );

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(placeDetailRepository.findByPlaceId(10L)).willReturn(Optional.of(placeDetail));
            given(reviewImageRepository.findAllByReviewIdOrderBySequence(reviewId))
                    .willReturn(List.of(oldImage));
            willThrow(new RuntimeException("MinIO 연결 실패"))
                    .given(fileStorage).deleteObjects(List.of("old-key-1"));

            // when — 예외 없이 정상 완료되어야 함
            reviewService.updateReview(userId, reviewId, request);

            // then
            assertThat(review.getRating()).isEqualTo(5.0);
            verify(fileStorage).deleteObjects(List.of("old-key-1"));
        }
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        @Test
        void 리뷰_삭제_성공() {
            // given
            Long userId = 1L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(userId);
            given(place.getId()).willReturn(10L);
            Review review = createReview(user, place);
            PlaceDetail placeDetail = mock(PlaceDetail.class);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
            given(placeDetailRepository.findByPlaceId(10L)).willReturn(Optional.of(placeDetail));

            // when
            reviewService.deleteReview(userId, reviewId);

            // then
            assertThat(review.getStatus()).isEqualTo(ReviewStatus.DELETED);
            verify(placeDetail).removeReviewScores(new ReviewScores(4.0, OutletScore.MANY.getScore(),
                    CrowdStatus.NORMAL.getScore(), SpaceSize.MEDIUM.getScore(), Mood.CALM.getScore()));
        }

        @Test
        void 타인의_리뷰_삭제_시_예외() {
            // given
            Long otherUserId = 2L;
            Long reviewId = 1L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(user.getId()).willReturn(1L);
            Review review = createReview(user, place);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(otherUserId, reviewId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_OWNER);
        }
    }
}
