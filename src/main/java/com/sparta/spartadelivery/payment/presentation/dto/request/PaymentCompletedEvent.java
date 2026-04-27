package com.sparta.spartadelivery.payment.presentation.dto.request;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        UUID paymentId,
        Integer amount
) {
}
