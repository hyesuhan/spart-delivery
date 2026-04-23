package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        @NotNull
        UUID storeId,

        @NotNull
        UUID addressId,

        @NotNull
        OrderType orderType,

        String request,

        @NotNull @Valid
        List<OrderItemRequest> orderItems
) {


}
