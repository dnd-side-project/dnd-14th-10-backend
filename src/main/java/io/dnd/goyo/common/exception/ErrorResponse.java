package io.dnd.goyo.common.exception;

import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldError> errors
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors);
    }

    public record FieldError(
        String field,
        String reason
    ) {
    }
}
