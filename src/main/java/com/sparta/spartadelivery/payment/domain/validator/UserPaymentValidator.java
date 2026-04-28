package com.sparta.spartadelivery.payment.domain.validator;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPaymentValidator {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Integer validCreatePaymentAndGetAmount(Long userId, UUID orderId) {

        Order order = findOrderById(orderId);

        if (!Objects.equals(order.getCustomerId(), userId)) {
            throw new AppException(PayErrorCode.PAYMENT_NOT_FOUND);
        }

        return order.getTotalPrice();
    }

    private UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new AppException(AuthErrorCode.USER_NOT_FOUND));
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    public boolean checkAdmin(Long userId) {
        UserEntity user = findUserById(userId);

        Role userRole = user.getRole();

        if (userRole == Role.MANAGER || userRole == Role.MASTER) {
            return true;
        } else if (userRole == Role.CUSTOMER) {
            return false;
        } else {
            // 다 아니라면 exception
            throw new AppException(PayErrorCode.NO_ACCESS_PERMISSION);
        }
    }

    public boolean isValidGetInfo(Long userId, String createdBy) {
        UserEntity user = findUserById(userId);
        Role userRole = user.getRole();

        // 1. master or admin true
        if (userRole == Role.MASTER || userRole == Role.MANAGER) {
            return true;
        } else if (userRole == Role.CUSTOMER) {
            return Objects.equals(user.getUsername(), createdBy);
        }

        return false;
    }
}
