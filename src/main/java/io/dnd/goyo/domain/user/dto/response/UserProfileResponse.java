package io.dnd.goyo.domain.user.dto.response;

import io.dnd.goyo.domain.user.entity.User;
import io.dnd.goyo.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "유저 프로필 응답")
public record UserProfileResponse(
        @Schema(description = "유저 ID", example = "1")
        Long id,

        @Schema(description = "이름", example = "김고작")
        String name,

        @Schema(description = "닉네임", example = "고작이")
        String nickname,

        @Schema(description = "생년월일", example = "1995-03-15")
        LocalDate birth,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImg,

        @Schema(description = "위치정보 동의 여부", example = "true")
        Boolean locationConsent,

        @Schema(description = "거주지 행정구역 코드", example = "1168010100")
        Long regionCode
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getNickname(),
                user.getBirth(),
                user.getGender(),
                user.getProfileImg(),
                user.getLocationConsent(),
                user.getRegionCode()
        );
    }
}
