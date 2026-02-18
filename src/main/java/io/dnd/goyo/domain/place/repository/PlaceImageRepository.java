package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.PlaceImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PlaceImage pi WHERE pi.place.id = :placeId")
    void deleteAllByPlaceId(@Param("placeId") Long placeId);

    List<PlaceImage> findAllByPlaceIdOrderBySequence(Long placeId);
}
