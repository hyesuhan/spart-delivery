package com.sparta.spartadelivery.area.presentation.dto.response;

import com.sparta.spartadelivery.area.domain.entity.Area;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record AreaDetailResponse(
        @Schema(description = "운영 지역 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        UUID id,

        @Schema(description = "운영 지역명", example = "광화문")
        String name,

        @Schema(description = "시/도", example = "서울특별시")
        String city,

        @Schema(description = "구/군", example = "종로구")
        String district,

        @Schema(description = "운영 지역 활성화 여부", example = "true")
        boolean active,

        @Schema(description = "운영 지역 생성일시", example = "2026-04-21T12:00:00")
        LocalDateTime createdAt
) {

    public static AreaDetailResponse from(Area area) {
        return new AreaDetailResponse(
                area.getId(),
                area.getName(),
                area.getCity(),
                area.getDistrict(),
                area.isActive(),
                area.getCreatedAt()
        );
    }
}
