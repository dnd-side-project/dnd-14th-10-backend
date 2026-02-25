package io.dnd.goyo.common.exception;

import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldError> errors,
    Object data
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, Object data) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null, data);
    }

    public record FieldError(
        String field,
        String reason
    ) {
    }
}
