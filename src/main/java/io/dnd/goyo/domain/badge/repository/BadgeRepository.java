package io.dnd.goyo.domain.badge.repository;

import io.dnd.goyo.domain.badge.entity.Badge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    Optional<Badge> findByCode(String code);

    List<Badge> findByCodeIn(List<String> codes);
}
