package com.sparta.spartadelivery.store.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StoreCreateRequest(
        @Schema(description = "가게 카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440010")
        @NotNull(message = "가게 카테고리 ID는 필수입니다.")
        UUID storeCategoryId,

        @Schema(description = "지역 ID", example = "550e8400-e29b-41d4-a716-446655440011")
        @NotNull(message = "지역 ID는 필수입니다.")
        UUID areaId,

        @Schema(description = "가게명", example = "스파르타 분식")
        @NotBlank(message = "가게명은 필수입니다.")
        @Size(max = 100, message = "가게명은 최대 100자까지 입력할 수 있습니다.")
        String name,

        @Schema(description = "가게 주소", example = "서울특별시 강남구 테헤란로 123")
        @NotBlank(message = "가게 주소는 필수입니다.")
        @Size(max = 255, message = "가게 주소는 최대 255자까지 입력할 수 있습니다.")
        String address,

        @Schema(description = "가게 연락처", example = "02-1234-5678")
        @Size(max = 20, message = "가게 연락처는 최대 20자까지 입력할 수 있습니다.")
        String phone
) {
}
