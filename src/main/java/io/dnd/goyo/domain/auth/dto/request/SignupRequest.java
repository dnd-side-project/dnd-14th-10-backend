package io.dnd.goyo.domain.auth.dto.request;

import io.dnd.goyo.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "회원가입 토큰은 필수입니다")
        String signupToken,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 30, message = "이름은 30자 이내여야 합니다")
        String name,

        @Schema(description = "닉네임", example = "고요한여행자")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 30, message = "닉네임은 30자 이내여야 합니다")
        String nickname,

        @Schema(description = "성별", example = "MALE")
        @NotNull(message = "성별은 필수입니다")
        Gender gender,

        @Schema(description = "생년월일", example = "1995-03-15")
        LocalDate birth,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImg,

        @Schema(description = "위치 정보 동의 여부", example = "true")
        Boolean locationConsent,

        @Schema(description = "거주지 행정구역 코드 (5자리)", example = "11680")
        Integer regionCode
) {
}
