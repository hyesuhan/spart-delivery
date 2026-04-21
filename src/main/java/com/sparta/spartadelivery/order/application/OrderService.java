package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.address.exception.AddressErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderItemRepository;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final UserRepository userRepository;

    @Transactional
    public UUID createdOrder(Long userId, OrderCreateRequest request) {

        // 1. 가게가 주문 가능한 상태인가?

        // 2. 주소가 배달 가능한 지역인가?

        // 3. 메뉴 정보 및 최신 가격 정보 스냅샷

        // 4. 총 주문 금액 계산

        // 5. Order 엔티티 저장

        // 6. OrderItem 발행


    }

    private UserEntity getUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(OrderErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
