package com.sparta.spartadelivery.payment.presentation.dto.request;

import com.sparta.spartadelivery.payment.domain.entity.PaymentMethod;

public record PaymentRequest(
        PaymentMethod method
) {
}
