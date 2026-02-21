package io.dnd.goyo.domain.tag.dto.response;

import io.dnd.goyo.domain.tag.entity.Tag;

public record TagResponse(
        Long id,
        String code,
        String name
) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getCode(), tag.getName());
    }
}
