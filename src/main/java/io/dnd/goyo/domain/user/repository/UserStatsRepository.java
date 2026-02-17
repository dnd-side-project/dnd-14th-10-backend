package io.dnd.goyo.domain.user.repository;

import io.dnd.goyo.domain.user.entity.UserStats;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {

    Optional<UserStats> findByUserId(Long userId);
}
