package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequest(

        @NotNull(message = "menuId는 필수입니다.")
        UUID menuId,

        @NotEmpty(message = "수량은 0 이상이어야 합니다.") @Min(0)
        Integer quantity
) {
        public OrderItem toEntity(UUID orderId, Integer currentPrice) {
                return OrderItem.builder()
                        .menuId(this.menuId)
                        .quantity(this.quantity)
                        .unitPrice(currentPrice)
                        .build();
        }
}
