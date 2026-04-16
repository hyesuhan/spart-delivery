package com.sparta.spartadelivery.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqLoginDto(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
