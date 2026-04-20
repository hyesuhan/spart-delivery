package com.sparta.spartadelivery.order.domain;

import com.sparta.spartadelivery.orderItem.domain.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderUseCase {

    public Integer calculateTotal(List<OrderItem> items) {
        return  items.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
}
