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
        void 닉네임이_30자를_초과하면_예외_발생() {
            // given
            User.UserBuilder builder = createValidUserBuilder()
                    .nickname("a".repeat(31));

            // when & then
            assertThatThrownBy(builder::build)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("닉네임은 30자 이내여야 합니다");
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
    }
}
