package io.dnd.goyo.domain.user.repository;

import io.dnd.goyo.domain.user.entity.UserWithdraw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWithdrawRepository extends JpaRepository<UserWithdraw, Long> {
}
