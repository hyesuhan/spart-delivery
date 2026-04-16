package com.sparta.spartadelivery.auth.presentation.dto.response;

import com.sparta.spartadelivery.user.domain.entity.Role;

public record ResLoginDto(
        String accessToken,
        String username,
        Role role
) {
}
