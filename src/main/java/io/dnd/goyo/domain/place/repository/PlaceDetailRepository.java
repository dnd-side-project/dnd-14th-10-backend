package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.PlaceDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceDetailRepository extends JpaRepository<PlaceDetail, Long> {

    Optional<PlaceDetail> findByPlaceId(Long placeId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaceDetail pd SET pd.wishCount = pd.wishCount + 1 WHERE pd.place.id = :placeId")
    void incrementWishCount(@Param("placeId") Long placeId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PlaceDetail pd SET pd.wishCount = pd.wishCount - 1 WHERE pd.place.id = :placeId AND pd.wishCount > 0")
    void decrementWishCount(@Param("placeId") Long placeId);
}
