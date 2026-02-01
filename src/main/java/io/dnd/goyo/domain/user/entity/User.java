package io.dnd.goyo.domain.user.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.user.enums.Gender;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.domain.user.enums.UserRole;
import io.dnd.goyo.domain.user.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 30, nullable = false, unique = true)
    private String nickname;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String profileImg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id", columnDefinition = "TEXT")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Builder
    private User(
            String name,
            String nickname,
            LocalDate birth,
            Gender gender,
            Integer age,
            String profileImg,
            Provider provider,
            String providerId,
            UserRole role
    ) {
        validateName(name);
        validateNickname(nickname);
        validateGender(gender);
        validateProvider(provider);
        validateRole(role);

        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.gender = gender;
        this.age = age;
        this.profileImg = profileImg;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    private static final int NAME_MAX_LENGTH = 30;
    private static final int NICKNAME_MAX_LENGTH = 30;

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이름은 필수입니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("이름은 %d자 이내여야 합니다.", NAME_MAX_LENGTH));
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임은 필수입니다.");
        }
        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("닉네임은 %d자 이내여야 합니다.", NICKNAME_MAX_LENGTH));
        }
    }

    private static void validateGender(Gender gender) {
        if (gender == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "성별은 필수입니다.");
        }
    }

    private static void validateProvider(Provider provider) {
        if (provider == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "소셜 로그인 제공자는 필수입니다.");
        }
    }

    private static void validateRole(UserRole role) {
        if (role == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "사용자 권한은 필수입니다.");
        }
    }
}
