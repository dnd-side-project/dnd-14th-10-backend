package io.dnd.goyo.domain.user.service;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.common.storage.FileStorage;
import io.dnd.goyo.domain.user.dto.response.NicknameCheckResponse;
import io.dnd.goyo.domain.user.dto.response.UserProfileResponse;
import io.dnd.goyo.domain.user.dto.response.WithdrawReasonResponse;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.entity.UserWithdraw;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.enums.WithdrawReason;
import io.dnd.goyo.domain.user.entity.UserStats;
import io.dnd.goyo.domain.user.repository.UserRepository;
import io.dnd.goyo.domain.user.repository.UserStatsRepository;
import io.dnd.goyo.domain.user.repository.UserWithdrawRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserWithdrawRepository userWithdrawRepository;
    private final FileStorage fileStorage;

    public UserProfileResponse getMyProfile(Long userId) {
        User user = userReader.getUser(userId);
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        return UserProfileResponse.from(user, userStats);
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
    public void updateProfileImg(Long userId, String objectKey) {
        User user = userReader.getUser(userId);
        String profileImg = objectKey != null ? fileStorage.generatePublicUrl(objectKey) : null;
        user.updateProfileImg(profileImg);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawReason reason, String detail) {
        User user = userReader.getUser(userId);
        UserWithdraw userWithdraw = UserWithdraw.of(user, reason, detail);
        userWithdrawRepository.save(userWithdraw);
        user.withdraw();
    }

    public List<WithdrawReasonResponse> getWithdrawReasons() {
        return Arrays.stream(WithdrawReason.values())
                .map(WithdrawReasonResponse::from)
                .toList();
    }
}
