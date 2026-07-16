package com.deutschhub.infrastructure.config;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.common.exception.ErrorCodeDetail;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final MessageUtils messageUtils;

    public GlobalExceptionHandler(MessageUtils messageUtils) {
        this.messageUtils = messageUtils;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        String message = getMessage(ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getErrorCode())
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorCodeDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toErrorCodeDetail)
                .toList();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);

        String message = messageUtils.getMessage(ErrorCode.UNCATEGORIZED_EXCEPTION);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode())
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private String getMessage(BusinessException ex) {
        if (ex.getArgs() != null && ex.getArgs().length > 0) {
            return messageUtils.getMessage(ex.getErrorCode(), ex.getArgs());
        }

        return messageUtils.getMessage(ex.getErrorCode());
    }

    private ErrorCodeDetail toErrorCodeDetail(FieldError fieldError) {
        return ErrorCodeDetail.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(getSafeRejectedValue(fieldError))
                .build();
    }

    private Object getSafeRejectedValue(FieldError fieldError) {
        if ("password".equalsIgnoreCase(fieldError.getField())) {
            return null;
        }

        return fieldError.getRejectedValue();
    }
}
