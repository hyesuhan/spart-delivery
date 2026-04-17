package com.sparta.spartadelivery.address.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressDetailInfo(
        UUID id,
        String alias,
        String address,
        String detail,
        String zipCode,
        boolean isDefault,
        LocalDateTime cratedAt
) {
}
