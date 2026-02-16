package io.dnd.goyo.domain.user.dto.request;

import io.dnd.goyo.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "성별 수정 요청")
public record UpdateGenderRequest(
        @Schema(description = "성별", example = "MALE")
        @NotNull(message = "성별은 필수입니다")
        Gender gender
) {
}
