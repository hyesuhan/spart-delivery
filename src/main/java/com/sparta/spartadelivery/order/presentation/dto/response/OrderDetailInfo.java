package com.sparta.spartadelivery.order.presentation.dto.response;

import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.OrderType;

import java.util.List;
import java.util.UUID;

public record OrderDetailInfo(
        UUID orderId,
        UUID addressId,
        String address,
        String addressDetail,
        OrderType orderType,
        OrderStatus orderStatus,
        String request,
        Integer totalPrice,
        List<OrderItemDetailInfo> infos

) {

    public record OrderItemDetailInfo(
            UUID menuId,
            String menuName,
            Integer quantity
    ) {
        public static OrderItemDetailInfo from(OrderItem orderItem) {
            return new OrderItemDetailInfo(orderItem.getMenuId(),
                    orderItem.getMenuName(),
                    orderItem.getQuantity());
        }
    }
    public static OrderDetailInfo from(Order order, String address, String addressDetail) {

        List<OrderItemDetailInfo> itemResponses = order.getOrderItems().stream()
                .map(OrderItemDetailInfo::from)
                .toList();

        return new OrderDetailInfo(
                order.getId(),
                order.getAddressId(),
                address,
                addressDetail,
                order.getOrderType(),
                order.getStatus(),
                order.getRequest(),
                order.getTotalPrice(),
                itemResponses
                );

    }
}
