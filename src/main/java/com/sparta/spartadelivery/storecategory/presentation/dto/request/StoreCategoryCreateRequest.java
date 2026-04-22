package com.sparta.spartadelivery.storecategory.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreCategoryCreateRequest(
        @Schema(description = "가게 카테고리명", example = "한식")
        @NotBlank(message = "가게 카테고리명은 필수입니다.")
        @Size(max = 50, message = "가게 카테고리명은 최대 50자까지 입력할 수 있습니다.")
        String name
) {
}
