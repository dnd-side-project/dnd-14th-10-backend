package io.dnd.goyo.domain.user.controller;

import io.dnd.goyo.domain.user.dto.request.UpdateBirthRequest;
import io.dnd.goyo.domain.user.dto.request.UpdateGenderRequest;
import io.dnd.goyo.domain.user.dto.request.UpdateLocationConsentRequest;
import io.dnd.goyo.domain.user.dto.request.UpdateNicknameRequest;
import io.dnd.goyo.domain.user.dto.request.UpdateProfileImageRequest;
import io.dnd.goyo.domain.user.dto.request.UpdateRegionRequest;
import io.dnd.goyo.domain.user.dto.request.WithdrawRequest;
import io.dnd.goyo.domain.user.dto.response.NicknameCheckResponse;
import io.dnd.goyo.domain.user.dto.response.UserProfileResponse;
import io.dnd.goyo.domain.user.dto.response.WithdrawReasonResponse;
import io.dnd.goyo.domain.user.service.UserService;
import io.dnd.goyo.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 API")
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "마이페이지 프로필 조회", description = "현재 로그인한 유저의 프로필 정보를 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserProfileResponse response = userService.getMyProfile(userDetails.userId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 탈퇴", description = "탈퇴 사유와 함께 현재 로그인한 유저의 계정을 탈퇴 처리합니다")
    @PostMapping("/me/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request
    ) {
        userService.withdraw(userDetails.userId(), request.reason(), request.detail());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "탈퇴 사유 목록 조회", description = "회원 탈퇴 시 선택 가능한 사유 목록을 조회합니다")
    @GetMapping("/withdraw-reasons")
    public ResponseEntity<List<WithdrawReasonResponse>> getWithdrawReasons() {
        List<WithdrawReasonResponse> response = userService.getWithdrawReasons();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 중복 검사", description = "닉네임이 사용 가능한지 확인합니다")
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckResponse> checkNickname(
            @RequestParam @NotBlank(message = "닉네임은 필수입니다") String nickname
    ) {
        NicknameCheckResponse response = userService.checkNickname(nickname);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 수정", description = "현재 로그인한 유저의 닉네임을 수정합니다")
    @PatchMapping("/me/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        userService.updateNickname(userDetails.userId(), request.nickname());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "성별 수정", description = "현재 로그인한 유저의 성별을 수정합니다")
    @PatchMapping("/me/gender")
    public ResponseEntity<Void> updateGender(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateGenderRequest request
    ) {
        userService.updateGender(userDetails.userId(), request.gender());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "생년월일 수정", description = "현재 로그인한 유저의 생년월일을 수정합니다")
    @PatchMapping("/me/birth")
    public ResponseEntity<Void> updateBirth(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBirthRequest request
    ) {
        userService.updateBirth(userDetails.userId(), request.birth());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "거주지 수정", description = "현재 로그인한 유저의 거주지를 수정합니다")
    @PatchMapping("/me/region")
    public ResponseEntity<Void> updateRegion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateRegionRequest request
    ) {
        userService.updateRegionCode(userDetails.userId(), request.regionCode());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "위치정보 동의 수정", description = "현재 로그인한 유저의 위치정보 동의 여부를 수정합니다")
    @PatchMapping("/me/location-consent")
    public ResponseEntity<Void> updateLocationConsent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateLocationConsentRequest request
    ) {
        userService.updateLocationConsent(userDetails.userId(), request.locationConsent());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로필 이미지 수정", description = "현재 로그인한 유저의 프로필 이미지를 수정합니다. null 전송 시 이미지가 삭제됩니다")
    @PatchMapping("/me/profile-image")
    public ResponseEntity<Void> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateProfileImageRequest request
    ) {
        userService.updateProfileImg(userDetails.userId(), request.objectKey());
        return ResponseEntity.noContent().build();
    }
}
