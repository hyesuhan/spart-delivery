package com.sparta.spartadelivery.order.application;

import java.util.UUID;

public record MenuSnapshot(
        UUID menuId,
        Integer unitPrice
) {
}
