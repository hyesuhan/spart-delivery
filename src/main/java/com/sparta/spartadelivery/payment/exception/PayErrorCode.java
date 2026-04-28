package com.sparta.spartadelivery.payment.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PayErrorCode implements BaseErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제 목록을 찾을 수 없습니다."),

    NO_ACCESS_PERMISSION(HttpStatus.FORBIDDEN, "해당 결제 목록에 접근할 수 없는 사용자입니다.")
    ;
    private final HttpStatus status;
    private final String message;


    PayErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
