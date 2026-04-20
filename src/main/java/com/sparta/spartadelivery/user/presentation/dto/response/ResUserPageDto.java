package com.sparta.spartadelivery.user.presentation.dto.response;

import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record ResUserPageDto(
        @Schema(description = "사용자 목록")
        List<ResUserListDto> content,
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size,
        @Schema(description = "전체 요소 수", example = "42")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,
        @Schema(description = "정렬 조건", example = "createdAt,DESC")
        String sort
) {

    public static ResUserPageDto from(Page<UserEntity> users, String sort) {
        return new ResUserPageDto(
                users.getContent().stream()
                        .map(ResUserListDto::from)
                        .toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                sort
        );
    }
}
