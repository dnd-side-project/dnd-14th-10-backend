package io.dnd.goyo.domain.auth.controller;

import io.dnd.goyo.domain.auth.dto.request.OAuthLoginRequest;
import io.dnd.goyo.domain.auth.dto.request.RefreshRequest;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.LoginResponse;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.TokenResponse;
import io.dnd.goyo.domain.auth.service.AuthService;
import io.dnd.goyo.domain.user.enums.Provider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "OAuth 로그인", description = "OAuth 인가 코드로 로그인/회원가입 처리")
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request
    ) {
        Provider oauthProvider = Provider.from(provider);
        OAuthLoginResponse response = authService.oauthLogin(oauthProvider, request.code());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입 완료", description = "신규 회원의 추가 정보 입력 후 회원가입 완료")
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@Valid @RequestBody SignupRequest request) {
        LoginResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 갱신")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }
}
