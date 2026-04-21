package com.sparta.spartadelivery.order.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements BaseErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문 ID입니다."),

    STORE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "주문 시 영업 중이 아니거나 존재하지 않습니다."),
    MENU_PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "주문 시 메뉴 가격이 실제와 다릅니다."),
    ORDER_CANCEL_TIMEOUT(HttpStatus.BAD_REQUEST, "주문 후 5분 이내에만 취소가 가능합니다."),

    ORDER_IMMUTABLE(HttpStatus.FORBIDDEN, "요청 사항은 접수 대기일 때만 수정이 가능합니다."),
    UNAUTHORIZED_ORDER_ACCESS(HttpStatus.FORBIDDEN, "본인의 주문만 접근이 가능합니다.")

    ;

    private final HttpStatus status;
    private final String message;


    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
