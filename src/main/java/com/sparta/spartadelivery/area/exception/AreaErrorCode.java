package com.sparta.spartadelivery.area.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AreaErrorCode implements BaseErrorCode {

    AREA_CREATE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "운영 지역을 등록할 권한이 없습니다."),
    DUPLICATE_AREA_NAME(HttpStatus.BAD_REQUEST, "이미 등록된 운영 지역명입니다.");

    private final HttpStatus status;
    private final String message;

    AreaErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
