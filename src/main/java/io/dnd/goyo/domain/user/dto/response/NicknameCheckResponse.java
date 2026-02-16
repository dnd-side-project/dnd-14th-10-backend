package io.dnd.goyo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 검사 응답")
public record NicknameCheckResponse(
        @Schema(description = "사용 가능 여부", example = "true") boolean available
) {
    public static NicknameCheckResponse from(boolean available) {
        return new NicknameCheckResponse(available);
    }
}
