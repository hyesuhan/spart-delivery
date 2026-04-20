package com.sparta.spartadelivery.order.presentation.dto.request;

public record OrderItemRequest(
        Long menuId,
        Integer quantity
) {
}
