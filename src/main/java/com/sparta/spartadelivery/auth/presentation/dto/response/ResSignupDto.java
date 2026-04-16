package com.sparta.spartadelivery.auth.presentation.dto.response;


import com.sparta.spartadelivery.user.domain.entity.Role;

import java.time.LocalDateTime;

public record ResSignupDto(
        Long id,
        String username,
        String nickname,
        String email,
        Role role,
        LocalDateTime createdAt
) {
}
