package com.sparta.spartadelivery.address.presentation.dto.response;

import com.sparta.spartadelivery.address.domain.entity.Address;

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

    // fromEntity
    public static AddressDetailInfo fromEntity(Address address) {
        return new AddressDetailInfo(
                address.getId(),
                address.getAlias(),
                address.getAddress(),
                address.getDetail(),
                address.getZipCode(),
                address.isDefault(),
                address.getCreatedAt()
        );
    }
}
