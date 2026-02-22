package io.dnd.goyo.domain.placetag.entity;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import java.util.List;
import java.util.Objects;

public record PlaceTags(List<Long> tagIds) {

    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 5;

    public PlaceTags {
        validateNotNull(tagIds);

        List<Long> uniqueTagIds = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        validateSize(uniqueTagIds.size());

        tagIds = uniqueTagIds;
    }

    public static PlaceTags from(List<Long> tagIds) {
        return new PlaceTags(tagIds);
    }

    private static void validateNotNull(List<Long> tagIds) {
        if (tagIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "태그 목록은 필수입니다.");
        }
    }

    private static void validateSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("태그는 %d~%d개 선택해야 합니다.", MIN_SIZE, MAX_SIZE));
        }
    }
}
