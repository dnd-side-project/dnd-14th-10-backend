package io.dnd.goyo.domain.tag.repository;

import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.enums.TagType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByType(TagType type);
}
