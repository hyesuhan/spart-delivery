package com.sparta.spartadelivery.payment.presentation.dto.request;

import java.util.UUID;

public record PaymentRequest(
        UUID orderId,
        Integer amount
) {
}
