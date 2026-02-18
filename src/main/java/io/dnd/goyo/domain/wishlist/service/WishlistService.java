package io.dnd.goyo.domain.wishlist.service;

import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    @Transactional
    public void deleteByPlaceId(Long placeId) {
        wishlistRepository.deleteAllByPlaceId(placeId);
    }
}