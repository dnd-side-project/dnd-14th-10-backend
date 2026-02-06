package io.dnd.goyo.domain.user.repository;

import io.dnd.goyo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
