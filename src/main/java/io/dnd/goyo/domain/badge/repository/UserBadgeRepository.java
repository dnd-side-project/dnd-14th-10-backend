package io.dnd.goyo.domain.badge.repository;

import io.dnd.goyo.domain.badge.entity.UserBadge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    List<UserBadge> findAllByUserId(Long userId);
}
