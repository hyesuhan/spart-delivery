package com.sparta.spartadelivery.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReqLoginDto(
        @Schema(description = "로그인 이메일", example = "user01@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "로그인 비밀번호", example = "Password1!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
