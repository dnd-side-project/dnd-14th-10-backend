package io.dnd.goyo.domain.tag.dto.response;

import io.dnd.goyo.domain.tag.entity.Tag;
import io.dnd.goyo.domain.tag.enums.TagType;

public record TagResponse(
        Long id,
        String code,
        String name,
        TagType type
) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getCode(), tag.getName(), tag.getType());
    }
}
