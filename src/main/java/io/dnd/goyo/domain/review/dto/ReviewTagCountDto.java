package io.dnd.goyo.domain.review.dto;

public record ReviewTagCountDto(Long tagId, String code, String name, long count) {
}
