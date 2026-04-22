package com.sparta.spartadelivery.storecategory.presentation.dto.response;

import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record StoreCategoryPageResponse(
        @Schema(description = "가게 카테고리 목록")
        List<StoreCategoryListResponse> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "10")
        int size,

        @Schema(description = "전체 요소 수", example = "12")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "2")
        int totalPages,

        @Schema(description = "정렬 조건", example = "createdAt,DESC")
        String sort
) {

    public static StoreCategoryPageResponse from(Page<StoreCategory> storeCategories, String sort) {
        return new StoreCategoryPageResponse(
                storeCategories.getContent().stream()
                        .map(StoreCategoryListResponse::from)
                        .toList(),
                storeCategories.getNumber(),
                storeCategories.getSize(),
                storeCategories.getTotalElements(),
                storeCategories.getTotalPages(),
                sort
        );
    }
}
