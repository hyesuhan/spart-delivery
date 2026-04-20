package com.sparta.spartadelivery.order.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을수 없습니다."),

    INVALID_ORDER_FORMAT(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 형식입니다."),
    MISSING_ORDER_DETAIL(HttpStatus.BAD_REQUEST, "필수 주문 정보가 누락되었습니다."),
    NOT_SERVICEABLE_AREA(HttpStatus.BAD_REQUEST, "주문 불가능 지역입니다."),

    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 주문에 대한 접근 권한이 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 삭제된 주문입니다."),

    ORDER_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문 조회 중 오류가 발생했습니다.")

    ;

    private final HttpStatus status;
    private final String message;


    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
