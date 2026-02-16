package io.dnd.goyo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 수정 요청")
public record UpdateProfileImageRequest(
        @Schema(description = "프로필 이미지 URL (null이면 이미지 삭제)", example = "https://example.com/profile.jpg")
        String profileImg
) {
}
