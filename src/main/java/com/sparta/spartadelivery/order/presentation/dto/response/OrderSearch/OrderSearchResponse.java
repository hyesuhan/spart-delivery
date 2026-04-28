package com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch;

import com.sparta.spartadelivery.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderSearchResponse(
        UUID orderId,
        Long customerId,
        UUID storeId,
        String storeName,
        OrderStatus status,
        String firstItemName, // 아이템 하나만 가져옵니다.
        String request,
        LocalDateTime createdAt
) {
}
