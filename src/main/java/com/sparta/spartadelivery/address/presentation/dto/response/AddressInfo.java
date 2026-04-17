package com.sparta.spartadelivery.address.presentation.dto.response;

import java.util.UUID;

public record AddressInfo(
        UUID id,
        String alias,
        String address
) {
}
