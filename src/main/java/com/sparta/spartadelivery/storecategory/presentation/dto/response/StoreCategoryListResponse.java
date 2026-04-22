package com.sparta.spartadelivery.storecategory.presentation.dto.response;

import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreCategoryListResponse(
        @Schema(description = "가게 카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        UUID id,

        @Schema(description = "가게 카테고리명", example = "한식")
        String name,

        @Schema(description = "가게 카테고리 생성 일시", example = "2026-04-22T12:00:00")
        LocalDateTime createdAt
) {

    public static StoreCategoryListResponse from(StoreCategory storeCategory) {
        return new StoreCategoryListResponse(
                storeCategory.getId(),
                storeCategory.getName(),
                storeCategory.getCreatedAt()
        );
    }
}
