package io.dnd.goyo.domain.user.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.dto.response.NicknameCheckResponse;
import io.dnd.goyo.domain.user.dto.response.UserProfileResponse;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserReader userReader;
    private final UserRepository userRepository;

    public UserProfileResponse getMyProfile(Long userId) {
        User user = userReader.getUser(userId);
        return UserProfileResponse.from(user);
    }

    public NicknameCheckResponse checkNickname(String nickname) {
        boolean exists = userRepository.existsByNicknameAndStatusNot(nickname, UserStatus.DELETED);
        return NicknameCheckResponse.from(!exists);
    }

    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = userReader.getUser(userId);
        if (user.getNickname().equals(nickname)) {
            return;
        }
        if (userRepository.existsByNicknameAndStatusNot(nickname, UserStatus.DELETED)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        user.updateNickname(nickname);
    }

    @Transactional
    public void updateGender(Long userId, Gender gender) {
        User user = userReader.getUser(userId);
        user.updateGender(gender);
    }

    @Transactional
    public void updateBirth(Long userId, LocalDate birth) {
        User user = userReader.getUser(userId);
        user.updateBirth(birth);
    }

    @Transactional
    public void updateRegionCode(Long userId, Long regionCode) {
        User user = userReader.getUser(userId);
        user.updateRegionCode(regionCode);
    }

    @Transactional
    public void updateLocationConsent(Long userId, Boolean locationConsent) {
        User user = userReader.getUser(userId);
        user.updateLocationConsent(locationConsent);
    }

    @Transactional
    public void updateProfileImg(Long userId, String profileImg) {
        User user = userReader.getUser(userId);
        user.updateProfileImg(profileImg);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userReader.getUser(userId);
        user.withdraw();
    }
}
