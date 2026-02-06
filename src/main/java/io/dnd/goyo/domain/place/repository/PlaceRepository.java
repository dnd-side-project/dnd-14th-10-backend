package io.dnd.goyo.domain.place.repository;

import io.dnd.goyo.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
