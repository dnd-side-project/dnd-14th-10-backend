package io.dnd.goyo.common.exception;

import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(new ErrorResponse(errorCode.getCode(), e.getMessage(), null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJsonException(HttpMessageNotReadableException e) {
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException e) {
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations()
            .stream()
            .map(violation -> new ErrorResponse.FieldError(
                extractFieldName(violation.getPropertyPath().toString()),
                violation.getMessage()
            ))
            .toList();

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors));
    }

    private String extractFieldName(String propertyPath) {
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage() != null ? error.getDefaultMessage() : "입력값이 올바르지 않습니다"
            ))
            .toList();

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, fieldErrors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();

        if (cause instanceof ConstraintViolationException cve) {
            String constraintName = cve.getConstraintName();
            if ("uk_users_nickname".equals(constraintName)) {
                return ResponseEntity
                    .status(ErrorCode.DUPLICATE_NICKNAME.getStatus())
                    .body(ErrorResponse.of(ErrorCode.DUPLICATE_NICKNAME));
            }
            if (constraintName != null && constraintName.startsWith("uk_")) {
                return ResponseEntity
                    .status(ErrorCode.DUPLICATE_RESOURCE.getStatus())
                    .body(ErrorResponse.of(ErrorCode.DUPLICATE_RESOURCE));
            }
        }

        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getStatus())
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
