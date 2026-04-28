package com.sparta.spartadelivery.payment.application;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.repository.PaymentRepository;
import com.sparta.spartadelivery.payment.domain.validator.UserPaymentValidator;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentRequest;
import com.sparta.spartadelivery.payment.presentation.dto.response.PaymentDetailResponse;
import com.sparta.spartadelivery.payment.presentation.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final UserPaymentValidator validator;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse processPayment(Long userId, UUID orderId, PaymentRequest request) {

        Integer amount = validator.validCreatePaymentAndGetAmount(userId, orderId);

        Payment payment = Payment.create(orderId, request.method(), amount);

        paymentRepository.save(payment);

        return PaymentResponse.of(payment);

    }

    public List<PaymentResponse> getAllPayments(Long userId) {
        boolean isAdmin = validator.checkAdmin(userId);
        // 1. Orders findBy userId

        if (isAdmin) {
            return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                    .stream().map(PaymentResponse::of).toList();
        }

        // 1. 해당 유저의 모든 주문 ID 조회
        List<Order> orders = orderRepository.findAllByCustomerId(userId);

        // 2. N+1 문제 방지를 위해
        List<UUID> orderIds = orders.stream().map(Order::getId).toList();

        if(orderIds.isEmpty()) return List.of();

        // 3. IN 절 -> 주문 ID 에 해당하는 결제 내역 1번 쿼리로 조회
        List<Payment> payments = paymentRepository.findAllByOrderIdIn(orderIds);

        return payments.stream()
                .map(PaymentResponse::of)
                .toList();

    }


    public PaymentDetailResponse getDetailPayment(Long userId, UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(PayErrorCode.PAYMENT_NOT_FOUND));

        if(validator.isValidGetInfo(userId, payment.getCreatedBy())) {
            return PaymentDetailResponse.of(payment);
        }

        throw new AppException(PayErrorCode.NO_ACCESS_PERMISSION);
    }

}


