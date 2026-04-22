package com.sparta.spartadelivery.order.domain.repository;

import com.sparta.spartadelivery.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, OrderQueryRepository {
    Optional<Order> findByIdAndDeletedAtIsNull(UUID orderId);
}
