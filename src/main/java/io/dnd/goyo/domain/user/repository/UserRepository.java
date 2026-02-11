package io.dnd.goyo.domain.user.repository;

import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByNickname(String nickname);
}
