package com.sparta.spartadelivery.orderItem.domain.repository;

import com.sparta.spartadelivery.orderItem.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
