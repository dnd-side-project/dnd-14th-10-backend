package io.dnd.goyo.domain.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.place.entity.Place;
import io.dnd.goyo.domain.place.entity.PlaceDetail;
import io.dnd.goyo.domain.place.repository.PlaceDetailRepository;
import io.dnd.goyo.domain.place.service.PlaceReader;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.service.UserReader;
import io.dnd.goyo.domain.wishlist.dto.request.WishlistAddRequest;
import io.dnd.goyo.domain.wishlist.dto.response.WishCountResponse;
import io.dnd.goyo.domain.wishlist.dto.response.WishlistItemResponse;
import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private PlaceDetailRepository placeDetailRepository;

    @Mock
    private UserReader userReader;

    @Mock
    private PlaceReader placeReader;

    @Nested
    @DisplayName("찜 추가")
    class AddWishlist {

        @Test
        void 찜_추가_성공() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            given(place.getId()).willReturn(placeId);
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            WishlistAddRequest request = new WishlistAddRequest(placeId);

            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId)).willReturn(place);
            given(wishlistRepository.existsByUserIdAndPlaceId(userId, placeId)).willReturn(false);
            given(placeDetailRepository.findByPlaceId(placeId)).willReturn(Optional.of(placeDetail));

            // when
            wishlistService.addWishlist(userId, request);

            // then
            verify(wishlistRepository).save(any(Wishlist.class));
            verify(placeDetail).incrementWishCount();
        }

        @Test
        void 중복_찜_시_예외() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            User user = mock(User.class);
            Place place = mock(Place.class);
            WishlistAddRequest request = new WishlistAddRequest(placeId);

            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId)).willReturn(place);
            given(wishlistRepository.existsByUserIdAndPlaceId(userId, placeId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> wishlistService.addWishlist(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        @Test
        void 존재하지_않는_공간_찜_시_예외() {
            // given
            Long userId = 1L;
            Long placeId = 999L;
            User user = mock(User.class);
            WishlistAddRequest request = new WishlistAddRequest(placeId);

            given(userReader.getUser(userId)).willReturn(user);
            given(placeReader.getPlace(placeId))
                    .willThrow(new BusinessException(ErrorCode.PLACE_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> wishlistService.addWishlist(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 찜 목록 조회")
    class GetMyWishlists {

        @Test
        void 내_찜_목록_조회_성공() {
            // given
            Long userId = 1L;
            Wishlist wishlist = mock(Wishlist.class);
            Place place = mock(Place.class);
            PlaceDetail placeDetail = mock(PlaceDetail.class);
            org.locationtech.jts.geom.Point location = mock(org.locationtech.jts.geom.Point.class);

            given(wishlist.getId()).willReturn(1L);
            given(wishlist.getPlace()).willReturn(place);
            given(place.getId()).willReturn(10L);
            given(place.getName()).willReturn("테스트 카페");
            given(place.getPlaceDetail()).willReturn(placeDetail);
            given(place.getLocation()).willReturn(location);
            given(place.getRegionCode()).willReturn(new io.dnd.goyo.domain.place.entity.RegionCode(11110L));
            given(location.getY()).willReturn(37.5);
            given(location.getX()).willReturn(127.0);

            PageRequest pageable = PageRequest.of(0, 10);
            Page<Wishlist> wishlistPage = new PageImpl<>(List.of(wishlist), pageable, 1);

            given(wishlistRepository.findAllByUserId(userId, pageable)).willReturn(wishlistPage);

            // when
            Page<WishlistItemResponse> result = wishlistService.getMyWishlists(userId, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).placeId()).isEqualTo(10L);
        }

        @Test
        void 찜_목록이_비어있으면_빈_페이지_반환() {
            // given
            Long userId = 1L;
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Wishlist> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(wishlistRepository.findAllByUserId(userId, pageable)).willReturn(emptyPage);

            // when
            Page<WishlistItemResponse> result = wishlistService.getMyWishlists(userId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("찜 삭제")
    class RemoveWishlist {

        @Test
        void 찜_삭제_성공() {
            // given
            Long userId = 1L;
            Long placeId = 10L;
            Wishlist wishlist = mock(Wishlist.class);
            PlaceDetail placeDetail = mock(PlaceDetail.class);

            given(wishlistRepository.findByUserIdAndPlaceId(userId, placeId))
                    .willReturn(Optional.of(wishlist));
            given(placeDetailRepository.findByPlaceId(placeId)).willReturn(Optional.of(placeDetail));

            // when
            wishlistService.removeWishlist(userId, placeId);

            // then
            verify(wishlistRepository).delete(wishlist);
            verify(placeDetail).decrementWishCount();
        }

        @Test
        void 미존재_찜_삭제_시_예외() {
            // given
            Long userId = 1L;
            Long placeId = 999L;

            given(wishlistRepository.findByUserIdAndPlaceId(userId, placeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistService.removeWishlist(userId, placeId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WISHLIST_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("찜 수 조회")
    class GetWishCount {

        @Test
        void 찜_수_조회_성공() {
            // given
            Long placeId = 10L;
            Place place = mock(Place.class);

            given(placeReader.getPlace(placeId)).willReturn(place);
            given(wishlistRepository.countByPlaceId(placeId)).willReturn(5);

            // when
            WishCountResponse response = wishlistService.getWishCount(placeId);

            // then
            assertThat(response.placeId()).isEqualTo(placeId);
            assertThat(response.wishCount()).isEqualTo(5);
        }
    }
}
