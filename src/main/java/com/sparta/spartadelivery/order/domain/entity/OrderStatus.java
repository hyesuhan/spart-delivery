package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import org.springframework.web.bind.annotation.DeleteMapping;

public enum OrderStatus {
    PENDING,    // 주문 접수 대기
    ACCEPTED,   // 주문 접수 완료
    PREPARING,  // 주문 준비 중
    OUT_FOR_DELIVERY, // 배달 중
    DELIVERED,  // 배달 완료
    CANCELED;    // 주문 취소


    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> ACCEPTED;
            case ACCEPTED -> PREPARING;
            case PREPARING -> OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> DELIVERED;
            case DELIVERED -> throw new AppException(OrderErrorCode.ALREADY_DELIVERED);
            case CANCELED -> throw new AppException(OrderErrorCode.ORDER_ALREADY_CANCLED);
        };
    }
}
