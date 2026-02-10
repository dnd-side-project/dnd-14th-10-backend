package io.dnd.goyo.domain.tag.repository;

import io.dnd.goyo.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
