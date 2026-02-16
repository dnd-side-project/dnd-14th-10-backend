package io.dnd.goyo.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.dto.response.NicknameCheckResponse;
import io.dnd.goyo.domain.user.dto.response.UserProfileResponse;
import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import io.dnd.goyo.domain.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserReader userReader;

    @Mock
    private UserRepository userRepository;

    private static User createValidUser() {
        return User.builder()
                .name("김고작")
                .nickname("고작이")
                .gender(Gender.MALE)
                .provider(Provider.KAKAO)
                .role(UserRole.USER)
                .locationConsent(true)
                .regionCode(1168010100L)
                .build();
    }

    @Nested
    @DisplayName("프로필 조회")
    class GetMyProfile {

        @Test
        void 프로필_조회_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            UserProfileResponse response = userService.getMyProfile(userId);

            // then
            assertThat(response.name()).isEqualTo("김고작");
            assertThat(response.nickname()).isEqualTo("고작이");
            assertThat(response.gender()).isEqualTo(Gender.MALE);
        }

        @Test
        void 존재하지_않는_사용자_조회_시_예외_발생() {
            // given
            Long userId = 999L;
            given(userReader.getUser(userId))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> userService.getMyProfile(userId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("닉네임 중복 검사")
    class CheckNickname {

        @Test
        void 사용_가능한_닉네임이면_available_true() {
            // given
            given(userRepository.existsByNicknameAndStatusNot("새닉네임", UserStatus.DELETED)).willReturn(false);

            // when
            NicknameCheckResponse response = userService.checkNickname("새닉네임");

            // then
            assertThat(response.available()).isTrue();
        }

        @Test
        void 중복된_닉네임이면_available_false() {
            // given
            given(userRepository.existsByNicknameAndStatusNot("고작이", UserStatus.DELETED)).willReturn(true);

            // when
            NicknameCheckResponse response = userService.checkNickname("고작이");

            // then
            assertThat(response.available()).isFalse();
        }
    }

    @Nested
    @DisplayName("닉네임 수정")
    class UpdateNickname {

        @Test
        void 닉네임_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);
            given(userRepository.existsByNicknameAndStatusNot("새닉네임", UserStatus.DELETED)).willReturn(false);

            // when
            userService.updateNickname(userId, "새닉네임");

            // then
            assertThat(user.getNickname()).isEqualTo("새닉네임");
        }

        @Test
        void 중복_닉네임으로_수정_시_예외_발생() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);
            given(userRepository.existsByNicknameAndStatusNot("중복닉네임", UserStatus.DELETED)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.updateNickname(userId, "중복닉네임"))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void 현재_닉네임과_동일하면_변경없이_정상_반환() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateNickname(userId, "고작이");

            // then
            assertThat(user.getNickname()).isEqualTo("고작이");
        }
    }

    @Nested
    @DisplayName("성별 수정")
    class UpdateGender {

        @Test
        void 성별_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateGender(userId, Gender.FEMALE);

            // then
            assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        }
    }

    @Nested
    @DisplayName("생년월일 수정")
    class UpdateBirth {

        @Test
        void 생년월일_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            LocalDate birth = LocalDate.of(1995, 3, 15);
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateBirth(userId, birth);

            // then
            assertThat(user.getBirth()).isEqualTo(birth);
        }
    }

    @Nested
    @DisplayName("거주지 수정")
    class UpdateRegionCode {

        @Test
        void 거주지_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateRegionCode(userId, 1111010100L);

            // then
            assertThat(user.getRegionCode()).isEqualTo(1111010100L);
        }
    }

    @Nested
    @DisplayName("위치정보 동의 수정")
    class UpdateLocationConsent {

        @Test
        void 위치정보_동의_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateLocationConsent(userId, false);

            // then
            assertThat(user.getLocationConsent()).isFalse();
        }
    }

    @Nested
    @DisplayName("프로필 이미지 수정")
    class UpdateProfileImg {

        @Test
        void 프로필_이미지_수정_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateProfileImg(userId, "https://example.com/new-profile.jpg");

            // then
            assertThat(user.getProfileImg()).isEqualTo("https://example.com/new-profile.jpg");
        }

        @Test
        void 프로필_이미지_삭제_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.updateProfileImg(userId, null);

            // then
            assertThat(user.getProfileImg()).isNull();
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class Withdraw {

        @Test
        void 회원_탈퇴_성공() {
            // given
            Long userId = 1L;
            User user = createValidUser();
            given(userReader.getUser(userId)).willReturn(user);

            // when
            userService.withdraw(userId);

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(user.getNickname()).startsWith("deleted_");
        }
    }
}
