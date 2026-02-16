package io.dnd.goyo.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "생년월일 수정 요청")
public record UpdateBirthRequest(
        @Schema(description = "생년월일", example = "1995-03-15")
        @NotNull(message = "생년월일은 필수입니다")
        LocalDate birth
) {
}
