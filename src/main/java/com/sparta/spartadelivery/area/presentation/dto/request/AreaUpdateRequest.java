package com.sparta.spartadelivery.area.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AreaUpdateRequest(
        @Schema(description = "운영 지역명", example = "광화문")
        @NotBlank(message = "지역명은 필수입니다.")
        @Size(max = 100, message = "지역명은 최대 100자까지 입력할 수 있습니다.")
        String name,

        @Schema(description = "시/도", example = "서울특별시")
        @NotBlank(message = "시/도는 필수입니다.")
        @Size(max = 50, message = "시/도는 최대 50자까지 입력할 수 있습니다.")
        String city,

        @Schema(description = "구/군", example = "종로구")
        @NotBlank(message = "구/군은 필수입니다.")
        @Size(max = 50, message = "구/군은 최대 50자까지 입력할 수 있습니다.")
        String district,

        @Schema(description = "운영 지역 활성화 여부", example = "true")
        @NotNull(message = "활성화 여부는 필수입니다.")
        Boolean active
) {
}
