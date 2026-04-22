package com.sparta.spartadelivery.area.presentation.dto.response;

import com.sparta.spartadelivery.area.domain.entity.Area;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record AreaPageResponse(
        @Schema(description = "운영 지역 목록")
        List<AreaListResponse> content,

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

    public static AreaPageResponse from(Page<Area> areas, String sort) {
        return new AreaPageResponse(
                areas.getContent().stream()
                        .map(AreaListResponse::from)
                        .toList(),
                areas.getNumber(),
                areas.getSize(),
                areas.getTotalElements(),
                areas.getTotalPages(),
                sort
        );
    }
}
