package io.dnd.goyo.domain.tag.entity;

import io.dnd.goyo.common.entity.BaseEntity;
import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.tag.enums.TagType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TagType type;

    @Column(length = 30, nullable = false)
    private String name;

    public static Tag of(TagType type, String name) {
        return new Tag(type, name);
    }

    private Tag(TagType type, String name) {
        validateType(type);
        validateName(name);
        this.type = type;
        this.name = name;
    }

    private static final int NAME_MAX_LENGTH = 30;

    private static void validateType(TagType type) {
        if (type == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 타입은 필수입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 이름은 필수입니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("태그 이름은 %d자 이내여야 합니다.", NAME_MAX_LENGTH));
        }
    }
}
