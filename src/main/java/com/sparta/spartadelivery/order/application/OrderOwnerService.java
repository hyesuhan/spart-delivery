package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderValidator;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderOwnerService {

    private final OrderValidator orderValidator;
    private final OrderRepository orderRepository;

    /** OWNER, MANAGER, MASTER CAN **/

    @Transactional
    public void updateOrderStatus(Long userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        orderValidator.validUpdateOrderStatus(userId, order);

        order.updateOrderStatus();
    }
}
