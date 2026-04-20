package com.sparta.spartadelivery.address.presentation.dto.response;

import com.sparta.spartadelivery.address.domain.entity.Address;

import java.util.UUID;

public record AddressInfo(
        UUID id,
        String alias,
        String address
) {

    // entity -> dto
    public static AddressInfo of(Address address) {
        return new AddressInfo(address.getId(), address.getAlias(), address.getAddress());
    }
}
