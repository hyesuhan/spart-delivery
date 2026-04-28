package com.sparta.spartadelivery.payment.presentation.dto.response;

import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentMethod;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDetailResponse (
        UUID paymentId,
        UUID orderId,
        Integer amount,
        PaymentMethod method,
        PaymentStatus status,
        LocalDateTime createdAt

) {

    public static PaymentDetailResponse of(Payment payment) {
        return new PaymentDetailResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getCreatedAt()
        );
    }
}
