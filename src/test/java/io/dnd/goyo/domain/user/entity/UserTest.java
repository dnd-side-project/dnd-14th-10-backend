package io.dnd.goyo.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    private static User.UserBuilder createValidUserBuilder() {
        return User.builder()
                .name("김고작")
                .nickname("고작이")
                .gender(Gender.MALE)
                .provider(Provider.KAKAO)
                .role(UserRole.USER);
    }

    @Nested
    @DisplayName("User 생성 시")
    class CreateUser {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 이름이_비어있으면_예외_발생(String name) {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .name(name);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이름은 필수입니다");
        }

        @Test
        void 이름이_30자를_초과하면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .name("a".repeat(31));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이름은 30자 이내여야 합니다");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 닉네임이_비어있으면_예외_발생(String nickname) {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .nickname(nickname);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("닉네임은 필수입니다");
        }

        @Test
        void 닉네임이_10자를_초과하면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .nickname("a".repeat(11));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("닉네임은 10자 이내여야 합니다");
        }

        @Test
        void 성별이_null이면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .gender(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("성별은 필수입니다");
        }

        @Test
        void 소셜_로그인_제공자가_null이면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .provider(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("소셜 로그인 제공자는 필수입니다");
        }

        @Test
        void 사용자_권한이_null이면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .role(null);

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자 권한은 필수입니다");
        }

        @Test
        void 정상적인_값으로_생성하면_상태는_ACTIVE() {
            // given & when
            User user = createValidUserBuilder().build();

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        void 정상적인_값으로_생성_가능() {
            // given & when
            User user = createValidUserBuilder()
                    .name("김고작")
                    .nickname("고작이")
                    .build();

            // then
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo("김고작");
            assertThat(user.getNickname()).isEqualTo("고작이");
        }

        @Test
        void locationConsent와_regionCode를_포함하여_생성_가능() {
            // given & when
            User user = createValidUserBuilder()
                    .locationConsent(true)
                    .regionCode(1168010100L)
                    .build();

            // then
            assertThat(user.getLocationConsent()).isTrue();
            assertThat(user.getRegionCode()).isEqualTo(1168010100L);
        }

        @Test
        void locationConsent와_regionCode가_null이어도_생성_가능() {
            // given & when
            User user = createValidUserBuilder()
                    .locationConsent(null)
                    .regionCode(null)
                    .build();

            // then
            assertThat(user.getLocationConsent()).isNull();
            assertThat(user.getRegionCode()).isNull();
        }
    }

    @Nested
    @DisplayName("닉네임 수정 시")
    class UpdateNickname {

        @Test
        void 정상적인_닉네임으로_수정_가능() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.updateNickname("새닉네임");

            // then
            assertThat(user.getNickname()).isEqualTo("새닉네임");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void 비어있는_닉네임으로_수정_시_예외_발생(String nickname) {
            // given
            User user = createValidUserBuilder().build();

            // when & then
            assertThatThrownBy(() -> user.updateNickname(nickname))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("닉네임은 필수입니다");
        }

        @Test
        void 닉네임이_10자를_초과하면_예외_발생() {
            // given
            User user = createValidUserBuilder().build();

            // when & then
            assertThatThrownBy(() -> user.updateNickname("a".repeat(11)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("닉네임은 10자 이내여야 합니다");
        }
    }

    @Nested
    @DisplayName("성별 수정 시")
    class UpdateGender {

        @Test
        void 정상적인_성별로_수정_가능() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.updateGender(Gender.FEMALE);

            // then
            assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        }

        @Test
        void null_성별로_수정_시_예외_발생() {
            // given
            User user = createValidUserBuilder().build();

            // when & then
            assertThatThrownBy(() -> user.updateGender(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("성별은 필수입니다");
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 시")
    class Withdraw {

        @Test
        void 정상_탈퇴_시_상태가_DELETED() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.withdraw();

            // then
            assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(user.getNickname()).startsWith("deleted_");
        }

        @Test
        void 이미_탈퇴한_사용자가_다시_탈퇴_시_예외_발생() {
            // given
            User user = createValidUserBuilder().build();
            user.withdraw();

            // when & then
            assertThatThrownBy(user::withdraw)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 탈퇴한 사용자입니다");
        }
    }

    @Nested
    @DisplayName("프로필 수정 시")
    class UpdateProfile {

        @Test
        void 생년월일_수정_가능() {
            // given
            User user = createValidUserBuilder().build();
            java.time.LocalDate birth = java.time.LocalDate.of(1995, 3, 15);

            // when
            user.updateBirth(birth);

            // then
            assertThat(user.getBirth()).isEqualTo(birth);
        }

        @Test
        void 거주지_수정_가능() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.updateRegionCode(1111010100L);

            // then
            assertThat(user.getRegionCode()).isEqualTo(1111010100L);
        }

        @Test
        void 위치정보_동의_수정_가능() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.updateLocationConsent(true);

            // then
            assertThat(user.getLocationConsent()).isTrue();
        }

        @Test
        void 프로필_이미지_수정_가능() {
            // given
            User user = createValidUserBuilder().build();

            // when
            user.updateProfileImg("https://example.com/profile.jpg");

            // then
            assertThat(user.getProfileImg()).isEqualTo("https://example.com/profile.jpg");
        }

        @Test
        void 프로필_이미지_null로_삭제_가능() {
            // given
            User user = createValidUserBuilder()
                    .profileImg("https://example.com/profile.jpg")
                    .build();

            // when
            user.updateProfileImg(null);

            // then
            assertThat(user.getProfileImg()).isNull();
        }
    }
}
