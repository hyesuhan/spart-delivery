package com.sparta.spartadelivery.global.exception;

import com.sparta.spartadelivery.global.exception.code.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements BaseErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL"),
    ROLE_REVALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "ROLE_REVALIDATION_FAILED"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
