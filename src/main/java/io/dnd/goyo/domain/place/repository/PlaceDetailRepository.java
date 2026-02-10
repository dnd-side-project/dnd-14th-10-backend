package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.PlaceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceDetailRepository extends JpaRepository<PlaceDetail, Long> {
}
