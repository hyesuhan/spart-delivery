package com.sparta.spartadelivery.order.domain.entity;

public enum OrderStatus {
    PENDING,    // 주문 접수 대기
    ACCEPTED,   // 주문 접수 완료
    PREPARING,  // 주문 준비 중
    OUT_FOR_DELIVERY, // 배달 중
    DELIVERED,  // 배달 완료
}
