package com.sparta.spartadelivery.menu.presentation.dto.response;

import com.sparta.spartadelivery.menu.domain.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record MenuDetailResponse(

        @Schema(description = "메뉴 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        UUID id,

        @Schema(description = "메뉴명", example = "불고기버거")
        String name,

        @Schema(description = "메뉴 가격", example = "5500")
        Integer price,

        @Schema(description = "메뉴 숨김 여부", example = "false")
        boolean hidden,

        @Schema(description = "메뉴 삭제 여부", example = "false")
        boolean deleted,

        @Schema(description = "메뉴 생성일시", example = "2026-04-21T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "메뉴 수정일시", example = "2026-04-21T12:00:00")
        LocalDateTime updatedAt
) {

    public static MenuDetailResponse from(Menu menu) {
        return new MenuDetailResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice().getPrice(),   // MoneyVO → price 값 꺼내기
                menu.isHidden(),
                menu.getDeletedAt() != null,
                menu.getCreatedAt(),
                menu.getUpdatedAt()
        );
    }
}