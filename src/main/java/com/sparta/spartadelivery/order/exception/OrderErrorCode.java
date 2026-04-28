package com.sparta.spartadelivery.order.exception;

import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements BaseErrorCode {
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문 ID입니다."),

    STORE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "주문 시 영업 중이 아니거나 존재하지 않습니다."),
    MENU_PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "주문 시 메뉴 가격이 실제와 다릅니다."),
    MENU_NAME_CHANGED(HttpStatus.BAD_REQUEST, "주문 시 메뉴 이름이 실제와 다릅니다."),
    ORDER_CANCEL_TIMEOUT(HttpStatus.BAD_REQUEST, "주문 후 5분 이내에만 취소가 가능합니다."),
    ORDER_CALCEL_ONLY_PENDING(HttpStatus.BAD_REQUEST, "주문 요청 사항은 주문 대기일 때만 가능합니다."),
    TOTAL_PRICE_OVER_ZERO(HttpStatus.BAD_REQUEST, "주문 가격은 0원 이상이어야 합니다."),

    ORDER_IMMUTABLE(HttpStatus.FORBIDDEN, "요청 사항은 접수 대기일 때만 수정이 가능합니다."),
    UNAUTHORIZED_ORDER_ACCESS(HttpStatus.FORBIDDEN, "본인의 주문만 접근이 가능합니다."),

    ALREADY_DELIVERED(HttpStatus.BAD_REQUEST, "이미 배달이 완료되었습니다."),

    ORDER_ALREADY_CANCLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),

    // query 관련

    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "SEARCH 시 가능한 페이지 수는 10, 30, 50입니다."),
    INVALID_SORT_QUERY(HttpStatus.BAD_REQUEST, "SEARCH 시 createdAt 또는 totalPrice 만 정렬이 가능합니다."),
    ORDER_SEARCH_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 검색 권한이 없습니다."),

    // Order Item 관련

    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 0보다 커야 합니다."),
    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "주문 추가 내역이 없습니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 메뉴가 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;


    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
