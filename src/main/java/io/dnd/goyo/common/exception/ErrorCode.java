package io.dnd.goyo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 오류가 발생했습니다"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_003", "이미 존재하는 데이터입니다"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "권한이 없습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다"),
    OAUTH_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_005", "OAuth 인증에 실패했습니다"),
    USER_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_006", "차단된 사용자입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "AUTH_007", "이미 사용중인 닉네임입니다"),
    INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_008", "유효하지 않은 회원가입 토큰입니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_002", "이미 탈퇴한 사용자입니다"),

    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE_001", "공간을 찾을 수 없습니다"),
    PLACE_NOT_OWNER(HttpStatus.FORBIDDEN, "PLACE_002", "공간 작성자만 수정/삭제할 수 있습니다"),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "리뷰를 찾을 수 없습니다"),
    REVIEW_NOT_OWNER(HttpStatus.FORBIDDEN, "REVIEW_002", "리뷰 작성자만 수정/삭제할 수 있습니다"),

    // Badge
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "BADGE_001", "뱃지를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
