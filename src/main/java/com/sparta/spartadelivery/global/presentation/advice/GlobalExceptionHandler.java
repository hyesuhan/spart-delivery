package com.sparta.spartadelivery.global.presentation.advice;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.global.presentation.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스 로직에서 의도적으로 던진 예외는 ErrorCode에 정의된 상태 코드로 응답한다.
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getStatus().value(), exception.getMessage(), null));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        List<ValidationErrorResponse> errors;

        // @RequestBody 검증 실패와 쿼리/폼 바인딩 실패는 BindingResult 위치가 달라서 각각 처리한다.
        if (exception instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            errors = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ValidationErrorResponse(error.getField(), error.getDefaultMessage()))
                    .toList();
        } else if (exception instanceof BindException bindException) {
            errors = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new ValidationErrorResponse(error.getField(), error.getDefaultMessage()))
                    .toList();
        } else {
            errors = List.of();
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(
                        ErrorCode.VALIDATION_ERROR.getStatus().value(),
                        ErrorCode.VALIDATION_ERROR.getMessage(),
                        errors
        ));
    }

    // Spring Security에서 인가 실패가 발생하면 공통 응답 형식으로 변환한다.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ignored) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ErrorCode.ACCESS_DENIED.getStatus().value(),
                        ErrorCode.ACCESS_DENIED.getMessage(),
                        null
        ));
    }

    // 예상하지 못한 예외는 내부 구현 메시지를 숨기고 공통 서버 오류로 응답한다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ignored) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        null
                ));
    }
}
