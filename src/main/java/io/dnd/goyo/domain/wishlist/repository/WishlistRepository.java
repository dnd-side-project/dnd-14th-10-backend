package io.dnd.goyo.domain.wishlist.repository;

import io.dnd.goyo.domain.wishlist.entity.Wishlist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("SELECT w.place.id FROM Wishlist w WHERE w.user.id = :userId AND w.place.id IN :placeIds")
    List<Long> findPlaceIdsByUserIdAndPlaceIds(@Param("userId") Long userId, @Param("placeIds") List<Long> placeIds);
}