package io.dnd.goyo.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.badge.enums.ActivityType;
import io.dnd.goyo.domain.badge.event.ActivityEvent;
import io.dnd.goyo.domain.history.event.PlaceViewedEvent;
import io.dnd.goyo.common.util.GeometryUtils;
import io.dnd.goyo.domain.place.dto.request.PlaceImageRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceRegisterRequest;
import io.dnd.goyo.domain.place.dto.request.PlaceUpdateRequest;
import io.dnd.goyo.domain.place.dto.response.MyPlaceResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceDetailResponse;
import io.dnd.goyo.domain.place.dto.response.PlaceMapItemResponse;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.entity.PlaceImage;
import io.dnd.goyo.domain.place.entity.RegionCode;
import io.dnd.goyo.domain.place.enums.CrowdStatus;
import io.dnd.goyo.domain.place.enums.Mood;
import io.dnd.goyo.domain.place.enums.OutletScore;
import io.dnd.goyo.domain.place.enums.PlaceCategory;
import io.dnd.goyo.domain.place.enums.PlaceSortType;
import io.dnd.goyo.domain.place.enums.PlaceStatus;
import io.dnd.goyo.domain.place.enums.SpaceSize;
import io.dnd.goyo.domain.place.repository.PlaceRepository;
import io.dnd.goyo.domain.placetag.service.PlaceTagService;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.service.WishlistReader;
import io.dnd.goyo.domain.wishlist.service.WishlistService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceDetailService placeDetailService;

    @Mock
    private PlaceTagService placeTagService;

    @Mock
    private UserReader userReader;

    @Mock
    private WishlistReader wishlistReader;

    @Mock
    private GeometryUtils geometryUtils;

    @Mock
    private PlaceImageService placeImageService;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private WishlistService wishlistService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Nested
    @DisplayName("공간 등록")
    class RegisterPlace {

        @Test
        void 공간_등록_성공() {
            // given
            Long userId = 1L;
            User user = mock(User.class);
            Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
            PlaceRegisterRequest request = createRegisterRequest();

            given(userReader.getUser(userId)).willReturn(user);
            given(geometryUtils.createPoint(request.longitude(), request.latitude())).willReturn(location);

            // when
            placeService.registerPlace(userId, request);

            // then
            verify(userReader).getUser(userId);
            verify(geometryUtils).createPoint(request.longitude(), request.latitude());
            verify(placeRepository).save(any(Place.class));
            verify(placeDetailService).registerPlaceDetail(any(PlaceDetail.class));
            verify(placeTagService).registerPlaceTags(any(Place.class), eq(request.tagIds()));
        }

        @Test
        void 존재하지_않는_사용자로_등록_시_예외_발생() {
            // given
            Long userId = 999L;
            PlaceRegisterRequest request = createRegisterRequest();

            given(userReader.getUser(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> placeService.registerPlace(userId, request))
                    .isInstanceOf(BusinessException.class);

            verify(placeRepository, never()).save(any(Place.class));
        }
    }

    @Nested
    @DisplayName("공간 상세 조회")
    class GetPlaceDetail {

        @Test
        void 공간_상세_조회_성공_모든_필드_검증() {
            // given
            Long userId = 1L;
            Long placeId = 1L;
            Place place = createMockPlace(placeId);

            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));
            given(wishlistReader.isWished(userId, placeId)).willReturn(true);
            given(fileStorage.generatePublicUrl("place/image1.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image1.jpg");

            // when
            PlaceDetailResponse response = placeService.getPlaceDetail(userId, placeId);

            // then
            assertThat(response.id()).isEqualTo(placeId);
            assertThat(response.name()).isEqualTo("테스트 카페");
            assertThat(response.category()).isEqualTo(PlaceCategory.CAFE);
            assertThat(response.averageRating()).isEqualTo(4.5);
            assertThat(response.reviewCount()).isEqualTo(10);
            assertThat(response.spaceSize()).isEqualTo(SpaceSize.LARGE);
            assertThat(response.mood()).isEqualTo(Mood.CALM);
            assertThat(response.outletScore()).isEqualTo(OutletScore.MANY);
            assertThat(response.crowdStatus()).isEqualTo(CrowdStatus.RELAX);
            assertThat(response.isWished()).isTrue();
            assertThat(response.images()).hasSize(1);
            assertThat(response.images().getFirst().url())
                    .isEqualTo("http://localhost:9000/goyo-local/place/image1.jpg");

            verify(placeRepository).findByIdWithDetails(placeId);
            verify(wishlistReader).isWished(userId, placeId);
            verify(eventPublisher).publishEvent(any(PlaceViewedEvent.class));
        }

        @Test
        void 존재하지_않는_공간_조회_예외() {
            // given
            Long placeId = 999L;
            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> placeService.getPlaceDetail(1L, placeId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);

            verify(placeRepository).findByIdWithDetails(placeId);
        }

        @Test
        void 이미지_URL_변환_확인() {
            // given
            Long placeId = 1L;
            Place place = createMockPlace(placeId);

            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));
            given(fileStorage.generatePublicUrl("place/image1.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image1.jpg");

            // when
            PlaceDetailResponse response = placeService.getPlaceDetail(1L, placeId);

            // then
            assertThat(response.images()).hasSize(1);
            assertThat(response.images().getFirst().url())
                    .isEqualTo("http://localhost:9000/goyo-local/place/image1.jpg");
            verify(fileStorage).generatePublicUrl("place/image1.jpg");
        }

        private Place createMockPlace(Long placeId) {
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            given(placeDetail.getAverageRating()).willReturn(4.5);
            given(placeDetail.getReviewCount()).willReturn(10);
            given(placeDetail.getSpaceSize()).willReturn(SpaceSize.LARGE);
            given(placeDetail.getMood()).willReturn(Mood.CALM);
            given(placeDetail.getOutletScore()).willReturn(OutletScore.MANY);
            given(placeDetail.getCrowdStatus()).willReturn(CrowdStatus.RELAX);

            PlaceImage image = mock(PlaceImage.class);
            given(image.getImageKey()).willReturn("place/image1.jpg");

            Place place = mock(Place.class);
            given(place.getId()).willReturn(placeId);
            given(place.getName()).willReturn("테스트 카페");
            given(place.getCategory()).willReturn(PlaceCategory.CAFE);
            given(place.getAddressDetail()).willReturn("서울시 강남구 테헤란로 123");
            given(place.getOpenTime()).willReturn(LocalTime.of(9, 0));
            given(place.getCloseTime()).willReturn(LocalTime.of(22, 0));
            given(place.getFloorInfo()).willReturn(1);
            given(place.getRestroomInfo()).willReturn("내부");
            given(place.getPlaceDetail()).willReturn(placeDetail);
            given(place.getImages()).willReturn(List.of(image));

            Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
            given(place.getLocation()).willReturn(location);

            return place;
        }
    }

    @Nested
    @DisplayName("공간 수정")
    class UpdatePlace {

        @Test
        void 공간_수정_성공() {
            // given
            Long userId = 1L;
            Long placeId = 1L;
            PlaceUpdateRequest request = createUpdateRequest();

            User user = mock(User.class);
            given(user.getId()).willReturn(userId);

            PlaceDetail placeDetail = mock(PlaceDetail.class);
            Place place = mock(Place.class);
            given(place.getUser()).willReturn(user);
            given(place.getPlaceDetail()).willReturn(placeDetail);
            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));

            // when
            placeService.updatePlace(userId, placeId, request);

            // then
            verify(place).update(request.name(), request.floorInfo(), request.openTime(), request.closeTime(), request.restroomInfo());
            verify(placeDetailService).updateScore(placeDetail, request.mood(), request.spaceSize(), request.outletScore(), request.crowdStatus());
            verify(placeTagService).replacePlaceTags(place, request.tagIds());
            verify(placeImageService).replaceImages(place, placeId, request.images());
        }

        @Test
        void 존재하지_않는_공간_수정_시_예외_발생() {
            // given
            given(placeRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> placeService.updatePlace(1L, 999L, createUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
        }

        @Test
        void 다른_사용자가_수정_시도_시_예외_발생() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;
            Long placeId = 1L;

            User owner = mock(User.class);
            given(owner.getId()).willReturn(ownerId);

            Place place = mock(Place.class);
            given(place.getUser()).willReturn(owner);
            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));

            // when & then
            assertThatThrownBy(() -> placeService.updatePlace(otherUserId, placeId, createUpdateRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_OWNER);
        }

        private PlaceUpdateRequest createUpdateRequest() {
            return new PlaceUpdateRequest(
                    "수정된 카페",
                    2,
                    LocalTime.of(10, 0),
                    LocalTime.of(22, 0),
                    "1층",
                    Mood.CALM,
                    SpaceSize.LARGE,
                    OutletScore.MANY,
                    CrowdStatus.RELAX,
                    List.of(1L, 2L),
                    List.of(new PlaceImageRequest("image.jpg", 0, true))
            );
        }
    }

    @Nested
    @DisplayName("공간 삭제")
    class DeletePlace {

        @Test
        void 공간_삭제_성공() {
            // given
            Long userId = 1L;
            Long placeId = 1L;

            User user = mock(User.class);
            given(user.getId()).willReturn(userId);

            Place place = mock(Place.class);
            given(place.getUser()).willReturn(user);
            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));

            // when
            placeService.deletePlace(userId, placeId);

            // then
            verify(placeImageService).deleteAllImages(place, placeId);
            verify(place).delete();
            verify(wishlistService).deleteByPlaceId(placeId);
            verify(eventPublisher).publishEvent(
                    new ActivityEvent(userId, ActivityType.PLACE, -1, -1)
            );
        }

        @Test
        void 존재하지_않는_공간_삭제_시_예외_발생() {
            // given
            given(placeRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> placeService.deletePlace(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
        }

       @Test
        void 다른_사용자가_삭제_시도_시_예외_발생() {
            // given
            Long ownerId = 1L;
            Long otherUserId = 2L;
            Long placeId = 1L;

            User owner = mock(User.class);
            given(owner.getId()).willReturn(ownerId);

            Place place = mock(Place.class);
            given(place.getUser()).willReturn(owner);
            given(placeRepository.findByIdWithDetails(placeId)).willReturn(Optional.of(place));

            // when & then
            assertThatThrownBy(() -> placeService.deletePlace(otherUserId, placeId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_OWNER);

            verify(place, never()).delete();
            verify(wishlistService, never()).deleteByPlaceId(any());
        }
    }

    @Nested
    @DisplayName("공간 일괄 조회")
    class GetPlacesByIds {

        @Test
        void 정상_조회() {
            // given
            List<Long> ids = List.of(1L, 2L);
            Place place1 = createMockPlace(1L);
            Place place2 = createMockPlace(2L);

            given(placeRepository.findAllByIdWithDetails(ids)).willReturn(List.of(place1, place2));
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            List<PlaceMapItemResponse> responses = placeService.getPlacesByIds(ids);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.getFirst().images().getFirst().url())
                    .isEqualTo("http://localhost:9000/goyo-local/place/image.jpg");
        }

        @Test
        void 존재하지_않는_ID는_결과에서_제외() {
            // given
            List<Long> ids = List.of(1L, 999L);
            Place place1 = createMockPlace(1L);

            given(placeRepository.findAllByIdWithDetails(ids)).willReturn(List.of(place1));
            given(fileStorage.generatePublicUrl("place/image.jpg")).willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            List<PlaceMapItemResponse> responses = placeService.getPlacesByIds(ids);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().id()).isEqualTo(1L);
        }

        private Place createMockPlace(Long id) {
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            given(placeDetail.getMood()).willReturn(Mood.CALM);
            given(placeDetail.getSpaceSize()).willReturn(SpaceSize.MEDIUM);
            given(placeDetail.getWishCount()).willReturn(5);

            PlaceImage image = mock(PlaceImage.class);
            given(image.getImageKey()).willReturn("place/image.jpg");
            given(image.getSequence()).willReturn(0);
            given(image.isRepresentativeFlag()).willReturn(true);

            Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));

            Place place = mock(Place.class);
            given(place.getId()).willReturn(id);
            given(place.getName()).willReturn("테스트 카페 " + id);
            given(place.getCategory()).willReturn(PlaceCategory.CAFE);
            given(place.getAddressDetail()).willReturn("서울시 강남구");
            given(place.getRegionCode()).willReturn(new RegionCode(1168010100L));
            given(place.getLocation()).willReturn(location);
            given(place.getImages()).willReturn(new ArrayList<>(List.of(image)));
            given(place.getPlaceDetail()).willReturn(placeDetail);

            return place;
        }
    }

    @Nested
    @DisplayName("내 등록 공간 조회")
    class GetMyPlaces {

        @Test
        void 최신순_정렬로_내_공간_목록_조회_성공() {
            // given
            Long userId = 1L;
            Place place = createMockPlace(1L, "place/image.jpg");
            PageRequest pageable = PageRequest.of(0, 10);
            PageRequest sorted = pageable.withSort(PlaceSortType.LATEST.toSort());
            Page<Place> placePage = new PageImpl<>(List.of(place), sorted, 1);

            given(placeRepository.findAllByUserIdAndStatus(userId, PlaceStatus.ACTIVE, sorted))
                    .willReturn(placePage);
            given(wishlistReader.getWishedPlaceIdSet(userId, List.of(1L))).willReturn(Set.of(1L));
            given(fileStorage.generatePublicUrl("place/image.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            Page<MyPlaceResponse> result = placeService.getMyPlaces(userId, pageable, PlaceSortType.LATEST);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeId()).isEqualTo(1L);
            assertThat(result.getContent().get(0).wished()).isTrue();
            assertThat(result.getContent().get(0).representativeImageUrl())
                    .isEqualTo("http://localhost:9000/goyo-local/place/image.jpg");
            verify(placeRepository).findAllByUserIdAndStatus(userId, PlaceStatus.ACTIVE, sorted);
        }

        @Test
        void 인기순_정렬로_내_공간_목록_조회_성공() {
            // given
            Long userId = 1L;
            Place place = createMockPlace(1L, "place/image.jpg");
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Place> placePage = new PageImpl<>(List.of(place), pageable, 1);

            given(placeRepository.findAllByUserIdOrderByWishCountDesc(userId, pageable))
                    .willReturn(placePage);
            given(wishlistReader.getWishedPlaceIdSet(userId, List.of(1L))).willReturn(Set.of());
            given(fileStorage.generatePublicUrl("place/image.jpg"))
                    .willReturn("http://localhost:9000/goyo-local/place/image.jpg");

            // when
            Page<MyPlaceResponse> result = placeService.getMyPlaces(userId, pageable, PlaceSortType.POPULAR);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).wished()).isFalse();
            verify(placeRepository).findAllByUserIdOrderByWishCountDesc(userId, pageable);
        }

        @Test
        void 공간_목록이_비어있으면_빈_페이지_반환() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);
            PageRequest sorted = pageable.withSort(PlaceSortType.LATEST.toSort());
            Page<Place> emptyPage = new PageImpl<>(List.of(), sorted, 0);

            given(placeRepository.findAllByUserIdAndStatus(userId, PlaceStatus.ACTIVE, sorted))
                    .willReturn(emptyPage);
            given(wishlistReader.getWishedPlaceIdSet(userId, List.of())).willReturn(Set.of());

            // when
            Page<MyPlaceResponse> result = placeService.getMyPlaces(userId, pageable, PlaceSortType.LATEST);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        private Place createMockPlace(Long id, String imageKey) {
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            given(placeDetail.getMood()).willReturn(Mood.CALM);
            given(placeDetail.getSpaceSize()).willReturn(SpaceSize.MEDIUM);
            given(placeDetail.getWishCount()).willReturn(5);

            Place place = mock(Place.class);
            given(place.getId()).willReturn(id);
            given(place.getName()).willReturn("테스트 카페 " + id);
            given(place.getRepresentativeImageKey()).willReturn(imageKey);
            given(place.getPlaceDetail()).willReturn(placeDetail);

            return place;
        }
    }

    private static PlaceRegisterRequest createRegisterRequest() {
        return new PlaceRegisterRequest(
                "테스트 카페",
                PlaceCategory.CAFE,
                37.5,
                127.0,
                2,
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                1111010100L,
                "가온로 245",
                "1층",
                OutletScore.MANY,
                SpaceSize.LARGE,
                CrowdStatus.RELAX,
                Mood.CALM,
                List.of(1L, 2L),
                List.of(new PlaceImageRequest("image.jpg", 0, true))
        );
    }
}
