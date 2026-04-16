package com.sparta.spartadelivery.auth.presentation.dto.request;

import com.sparta.spartadelivery.user.domain.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ReqSignupDto(
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]{4,10}$", message = "4~10자의 알파벳 소문자 및 숫자만 사용 가능합니다.")
        String username,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,15}$",
                message = "8~15자의 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password,

        @NotBlank
        String nickname,

        @NotBlank
        @Email
        String email,

        @NotNull
        Role role
) {
}
