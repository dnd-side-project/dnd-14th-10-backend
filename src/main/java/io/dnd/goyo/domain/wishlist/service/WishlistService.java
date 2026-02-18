package io.dnd.goyo.domain.wishlist.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PlaceDetailRepository placeDetailRepository;
    private final UserReader userReader;
    private final PlaceReader placeReader;

    @Transactional
    public Long addWishlist(Long userId, WishlistAddRequest request) {
        User user = userReader.getUser(userId);
        Place place = placeReader.getPlace(request.placeId());

        if (wishlistRepository.existsByUserIdAndPlaceId(userId, request.placeId())) {
            throw new BusinessException(ErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        Wishlist wishlist = Wishlist.of(user, place);
        wishlistRepository.save(wishlist);

        PlaceDetail placeDetail = getPlaceDetail(place.getId());
        placeDetail.incrementWishCount();

        return wishlist.getId();
    }

    public Page<WishlistItemResponse> getMyWishlists(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistPage = wishlistRepository.findAllByUserId(userId, pageable);
        return wishlistPage.map(WishlistItemResponse::from);
    }

    @Transactional
    public void removeWishlist(Long userId, Long placeId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndPlaceId(userId, placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WISHLIST_NOT_FOUND));

        wishlistRepository.delete(wishlist);

        PlaceDetail placeDetail = getPlaceDetail(placeId);
        placeDetail.decrementWishCount();
    }

    public WishCountResponse getWishCount(Long placeId) {
        placeReader.getPlace(placeId);
        int count = wishlistRepository.countByPlaceId(placeId);
        return WishCountResponse.of(placeId, count);
    }

    private PlaceDetail getPlaceDetail(Long placeId) {
        return placeDetailRepository.findByPlaceId(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "공간 상세 정보가 누락되었습니다."));
    }
}
