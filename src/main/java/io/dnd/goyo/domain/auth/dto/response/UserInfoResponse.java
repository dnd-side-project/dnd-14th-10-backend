package io.dnd.goyo.domain.auth.dto.response;

import io.dnd.goyo.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserInfoResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "닉네임", example = "고요한여행자")
        String nickname,

        @Schema(description = "프로필 이미지 URL")
        String profileImg
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImg()
        );
    }
}
