package com.sparta.spartadelivery.address.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressUpdateRequest(
        String alias,

        @NotBlank(message = "주소는 필수 입력사항 입니다.")
        String address,
        String detail,
        String zipCode,
        boolean isDefault;
) {
}
