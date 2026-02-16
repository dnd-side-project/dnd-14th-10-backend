package io.dnd.goyo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "거주지 수정 요청")
public record UpdateRegionRequest(
        @Schema(description = "거주지 행정구역 코드 (10자리)", example = "1168010100")
        @NotNull(message = "거주지 코드는 필수입니다")
        Long regionCode
) {
}
