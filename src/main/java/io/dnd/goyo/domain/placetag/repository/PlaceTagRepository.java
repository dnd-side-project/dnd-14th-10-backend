package io.dnd.goyo.domain.placetag.repository;

import io.dnd.goyo.domain.placetag.entity.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, Long> {
}
