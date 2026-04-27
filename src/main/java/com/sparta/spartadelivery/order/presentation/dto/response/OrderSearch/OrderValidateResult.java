package com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.order.domain.entity.Order;

public record OrderValidateResult(
        Order order,
        Address address
) {
}
