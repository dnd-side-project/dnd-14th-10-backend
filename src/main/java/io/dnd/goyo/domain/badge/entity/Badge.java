package io.dnd.goyo.domain.badge.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "badges")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String code;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 100)
    private String description;

    @Builder
    private Badge(String code, String name, String description) {
        validateCode(code);
        validateName(name);

        this.code = code;
        this.name = name;
        this.description = description;
    }

    private static final int CODE_MAX_LENGTH = 30;
    private static final int NAME_MAX_LENGTH = 30;

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "뱃지 코드는 필수입니다.");
        }
        if (code.length() > CODE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("뱃지 코드는 %d자 이내여야 합니다.", CODE_MAX_LENGTH));
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "뱃지 이름은 필수입니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("뱃지 이름은 %d자 이내여야 합니다.", NAME_MAX_LENGTH));
        }
    }
}

