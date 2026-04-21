package com.sparta.spartadelivery.user.presentation.dto.response;

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record ResUserDetailDto(
        @Schema(description = "사용자 식별자", example = "1")
        Long id,
        @Schema(description = "사용자 이름", example = "user01")
        String username,
        @Schema(description = "사용자 닉네임", example = "유저01")
        String nickname,
        @Schema(description = "사용자 이메일", example = "user01@example.com")
        String email,
        @Schema(description = "사용자 권한", example = "CUSTOMER")
        Role role,
        @Schema(description = "프로필 공개 여부", example = "true")
        boolean isPublic,
        @Schema(description = "가입 일시", example = "2026-04-17T12:00:00")
        LocalDateTime createdAt,
        @Schema(description = "마지막 수정 일시", example = "2026-04-18T12:00:00")
        LocalDateTime updatedAt
) {

    // 사용자 엔티티를 상세 조회 응답 형식으로 변환한다.
    public static ResUserDetailDto from(UserEntity user) {
        return new ResUserDetailDto(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getRole(),
                user.isPublic(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
