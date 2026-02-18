package io.dnd.goyo.domain.auth.controller;

import io.dnd.goyo.common.exception.BusinessException;
import io.dnd.goyo.common.exception.ErrorCode;
import io.dnd.goyo.domain.auth.dto.AuthTokens;
import io.dnd.goyo.domain.auth.dto.request.OAuthLoginRequest;
import io.dnd.goyo.domain.auth.dto.request.SignupRequest;
import io.dnd.goyo.domain.auth.dto.response.LoginResponse;
import io.dnd.goyo.domain.auth.dto.response.OAuthLoginResponse;
import io.dnd.goyo.domain.auth.dto.response.TokenResponse;
import io.dnd.goyo.domain.auth.service.AuthService;
import io.dnd.goyo.domain.user.enums.Provider;
import io.dnd.goyo.security.cookie.RefreshTokenCookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final RefreshTokenCookieUtils cookieUtils;

    @Operation(
            summary = "OAuth 로그인",
            description = "OAuth 인가 코드로 로그인/회원가입 처리. 기존 사용자인 경우 `Set-Cookie: refresh_token` 헤더로 리프레시 토큰이 설정됩니다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    headers = @Header(name = "Set-Cookie", description = "refresh_token=<JWT>; Path=/api/auth; HttpOnly; SameSite=Lax", schema = @Schema(type = "string"))
            )
    )
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<OAuthLoginResponse> oauthLogin(
            @PathVariable String provider,
            @Valid @RequestBody OAuthLoginRequest request,
            HttpServletResponse response
    ) {
        Provider oauthProvider = Provider.from(provider);
        OAuthLoginResponse oAuthLoginResponse = authService.oauthLogin(
                oauthProvider, request.code(), request.redirectUri());

        if (!oAuthLoginResponse.isNewUser()) {
            cookieUtils.addRefreshTokenCookie(response, oAuthLoginResponse.refreshToken());
        }

        return ResponseEntity.ok(oAuthLoginResponse);
    }

    @Operation(
            summary = "회원가입 완료",
            description = "신규 회원의 추가 정보 입력 후 회원가입 완료. `Set-Cookie: refresh_token` 헤더로 리프레시 토큰이 설정됩니다.",
            responses = @ApiResponse(
                    responseCode = "201",
                    headers = @Header(name = "Set-Cookie", description = "refresh_token=<JWT>; Path=/api/auth; HttpOnly; SameSite=Lax", schema = @Schema(type = "string"))
            )
    )
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ) {
        AuthTokens authTokens = authService.signup(request);
        cookieUtils.addRefreshTokenCookie(response, authTokens.refreshToken());

        LoginResponse loginResponse = new LoginResponse(
                authTokens.accessToken(),
                authTokens.expiresIn(),
                authTokens.user()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @Operation(
            summary = "토큰 갱신",
            description = "쿠키의 리프레시 토큰으로 액세스 토큰을 갱신합니다. 리프레시 토큰은 rotation되어 새 쿠키로 설정됩니다.",
            parameters = @Parameter(name = "refresh_token", in = ParameterIn.COOKIE, description = "리프레시 토큰 (HttpOnly 쿠키로 자동 전송)", required = true, schema = @Schema(type = "string")),
            responses = @ApiResponse(
                    responseCode = "200",
                    headers = @Header(name = "Set-Cookie", description = "refresh_token=<새 JWT>; Path=/api/auth; HttpOnly; SameSite=Lax", schema = @Schema(type = "string"))
            )
    )
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.extractRefreshToken(request);
        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        AuthTokens authTokens = authService.refresh(refreshToken);
        cookieUtils.addRefreshTokenCookie(response, authTokens.refreshToken());

        return ResponseEntity.ok(new TokenResponse(authTokens.accessToken(), authTokens.expiresIn()));
    }

    @Operation(
            summary = "로그아웃",
            description = "리프레시 토큰을 무효화하고 쿠키를 삭제합니다.",
            parameters = @Parameter(name = "refresh_token", in = ParameterIn.COOKIE, description = "리프레시 토큰 (HttpOnly 쿠키로 자동 전송)", schema = @Schema(type = "string")),
            responses = @ApiResponse(
                    responseCode = "204",
                    headers = @Header(name = "Set-Cookie", description = "refresh_token=; Path=/api/auth; Max-Age=0 (쿠키 삭제)", schema = @Schema(type = "string"))
            )
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = cookieUtils.extractRefreshToken(request);
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        cookieUtils.clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
