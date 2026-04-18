package com.sparta.spartadelivery.auth.presentation.dto.response;

import com.sparta.spartadelivery.user.domain.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResLoginDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "사용자 이름", example = "홍길동")
        String username,
        @Schema(description = "사용자 권한", example = "CUSTOMER")
        Role role
) {
}
