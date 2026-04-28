package com.sparta.spartadelivery.payment.presentation.dto.response;

import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse (
        UUID paymentId,
        PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentResponse of(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
