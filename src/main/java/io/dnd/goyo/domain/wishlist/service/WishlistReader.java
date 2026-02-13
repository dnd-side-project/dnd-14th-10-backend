package io.dnd.goyo.domain.wishlist.service;

import io.dnd.goyo.domain.wishlist.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistReader {

    private final WishlistRepository wishlistRepository;

    public List<Long> getWishedPlaceIds(Long userId, List<Long> placeIds) {
        return wishlistRepository.findPlaceIdsByUserIdAndPlaceIds(userId, placeIds);
    }
}
