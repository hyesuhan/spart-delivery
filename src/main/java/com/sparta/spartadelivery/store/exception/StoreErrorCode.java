package com.sparta.spartadelivery.store.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StoreErrorCode implements BaseErrorCode {
    STORE_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "가게를 등록할 권한이 없습니다."),
    STORE_CREATE_OWNER_ROLE_REQUIRED(HttpStatus.FORBIDDEN, "가게는 OWNER 권한 사용자만 등록할 수 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    StoreErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
