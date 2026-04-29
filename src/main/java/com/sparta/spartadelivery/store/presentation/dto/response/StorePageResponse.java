package com.sparta.spartadelivery.store.presentation.dto.response;

import com.sparta.spartadelivery.store.domain.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record StorePageResponse(
        @Schema(description = "가게 목록")
        List<StoreListResponse> content,

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

    public static StorePageResponse from(Page<Store> stores, String sort) {
        return new StorePageResponse(
                stores.getContent().stream()
                        .map(StoreListResponse::from)
                        .toList(),
                stores.getNumber(),
                stores.getSize(),
                stores.getTotalElements(),
                stores.getTotalPages(),
                sort
        );
    }
}
