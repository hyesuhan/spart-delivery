package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.address.exception.AddressErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreatedOrderResponse createdOrder(Long userId, OrderCreateRequest request) {

        UserEntity user = getUser(userId);

        Order order = request.toEntity(user);
    }

    private UserEntity getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(OrderErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
