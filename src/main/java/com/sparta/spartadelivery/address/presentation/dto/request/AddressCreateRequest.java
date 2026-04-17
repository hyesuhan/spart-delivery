package com.sparta.spartadelivery.address.presentation.dto.request;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

public record AddressCreateRequest(

        String alias,

        @NotBlank(message = "주소는 필수 입력사항 입니다.")
        String address,

        String detail,
        String zipCode,
        boolean isDefault
) {

        public static Address of(UserEntity user, AddressCreateRequest request) {
                return Address.builder()
                        .user(user)
                        .alias(request.alias())
                        .address(request.address())
                        .detail(request.detail())
                        .zipCode(request.zipCode())
                        .isDefault(request.isDefault())
                        .build();
        }

}
