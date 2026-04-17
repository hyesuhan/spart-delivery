package com.sparta.spartadelivery.global.exception;

import com.sparta.spartadelivery.global.exception.code.BaseErrorCode;
import lombok.Getter;

// 서비스/인증/도메인 로직에서 의도적으로 던지는 Exception을 ErrorCode와 함께 표현하는 클래스
@Getter
public class AppException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public AppException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
