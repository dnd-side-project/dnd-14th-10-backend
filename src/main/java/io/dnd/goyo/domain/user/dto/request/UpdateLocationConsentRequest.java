package io.dnd.goyo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "위치정보 동의 수정 요청")
public record UpdateLocationConsentRequest(
        @Schema(description = "위치정보 동의 여부", example = "true")
        @NotNull(message = "위치정보 동의 여부는 필수입니다")
        Boolean locationConsent
) {
}
