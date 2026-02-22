package io.dnd.goyo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 수정 요청")
public record UpdateNicknameRequest(
        @Schema(description = "닉네임", example = "고요한여행자")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 10, message = "닉네임은 10자 이내여야 합니다")
        String nickname
) {
}
