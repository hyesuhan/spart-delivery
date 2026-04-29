package com.sparta.spartadelivery.store.presentation.dto.response;

import com.sparta.spartadelivery.store.domain.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StoreListResponse(
        @Schema(description = "가게 ID", example = "550e8400-e29b-41d4-a716-446655440012")
        UUID id,

        @Schema(description = "가게 카테고리명", example = "분식")
        String storeCategoryName,

        @Schema(description = "지역명", example = "강남")
        String areaName,

        @Schema(description = "가게명", example = "스파르타 분식")
        String name,

        @Schema(description = "평균 평점", example = "0.0")
        BigDecimal averageRating,

        @Schema(description = "숨김 여부", example = "false")
        boolean hidden,

        @Schema(description = "가게 생성 일시", example = "2026-04-24T12:00:00")
        LocalDateTime createdAt
) {

    public static StoreListResponse from(Store store) {
        return new StoreListResponse(
                store.getId(),
                store.getStoreCategory().getName(),
                store.getArea().getName(),
                store.getName(),
                store.getAverageRating(),
                store.isHidden(),
                store.getCreatedAt()
        );
    }
}
