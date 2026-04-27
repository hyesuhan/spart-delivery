package com.sparta.spartadelivery.order.application;


import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderValidator;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;


    /** ONLY CUSTOMER **/
    @Transactional
    public OrderResponse createOrder(Long customerId, OrderCreateRequest request) {

        orderValidator.validCreateOrder(customerId, request);

        // OrderItem
        List<OrderItem> orderItems = request.orderItems().stream()
                .map(item -> OrderItem.create(
                        item.menuId(),
                        item.menuName(),
                        item.quantity(),
                        item.unitPrice()))
                .toList();

        Order order = Order.create(
                customerId, request.storeId(), request.addressId(), orderItems,request.request()
        );

        orderRepository.save(order);


        return OrderResponse.from(order);
    }


    /** ONLY CUSTOMER **/
    @Transactional
    public void updateOrderRequest(Long customerId, UUID orderId, String updateRequest) {
        Order order = findOrderById(orderId);

        orderValidator.validUpdateRequest(customerId, order);

        order.updateRequest(updateRequest);
    }

    /** CUSTOMER & MASTER **/
    @Transactional
    public void cancelOrder(Long customerId, UUID orderId) {
        Order order = findOrderById(orderId);

        orderValidator.validCancelOrder(customerId, order);

        order.cancel(LocalDateTime.now()); // java clock 이랑 다른 점이 무엇이지?
    }

    /** ONLY MASTER **/
    @Transactional
    public void deleteOrder(Long requestUserId, UUID orderId) {
        Order order = findOrderById(orderId);

        String masterName = orderValidator.validDeleteOrderByMaster(requestUserId);

        order.markDeleted(masterName);
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId).
                orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public void markOrderAsPaid() {

    }

}
