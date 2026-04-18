package com.sparta.spartadelivery.user.presentation.dto.response;

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ResUpdateUserDto(
        @Schema(description = "사용자 식별자", example = "1")
        Long id,
        @Schema(description = "사용자 이름", example = "홍길동")
        String username,
        @Schema(description = "사용자 닉네임", example = "고길동")
        String nickname,
        @Schema(description = "사용자 이메일", example = "user01@example.com")
        String email,
        @Schema(description = "사용자 권한", example = "CUSTOMER")
        Role role,
        @Schema(description = "프로필 공개 여부", example = "true")
        boolean isPublic,
        @Schema(description = "마지막 수정 일시", example = "2026-04-18T12:00:00")
        LocalDateTime updatedAt
) {

    public static ResUpdateUserDto from(UserEntity user) {
        return new ResUpdateUserDto(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.isPublic(),
                user.getUpdatedAt()
        );
    }
}
