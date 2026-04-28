package com.sparta.spartadelivery.order.presentation.dto.response;

import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import com.sparta.spartadelivery.order.domain.entity.OrderType;

import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    UUID addressId,
    OrderType orderType,
    String request,
    List<OrderItemResponse> orderItemResponses

) {

    public record OrderItemResponse(
            UUID menuId,
            Integer quantity
    ) {
        public static OrderItemResponse from(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getMenuId(),
                    orderItem.getQuantity()
            );
        }
    }

    public static OrderResponse from(Order order) {

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(order.getId(),
                order.getAddressId(),
                order.getOrderType(),
                order.getRequest(),
                itemResponses
        );
    }

}
