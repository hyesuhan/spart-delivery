package com.sparta.spartadelivery.payment.presentation.dto.request;

import com.sparta.spartadelivery.payment.domain.entity.PaymentMethod;

import java.util.UUID;

public record PaymentRequest(
        PaymentMethod method
) {
}
