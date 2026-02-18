package io.dnd.goyo.domain.auth.service;

import io.dnd.goyo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenVersionService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementTokenVersionInNewTransaction(Long userId) {
        userRepository.incrementTokenVersion(userId);
    }
}
