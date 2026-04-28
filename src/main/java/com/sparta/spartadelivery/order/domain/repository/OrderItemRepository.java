package com.sparta.spartadelivery.order.domain.repository;

import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

}
