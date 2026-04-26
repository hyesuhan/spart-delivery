package com.sparta.spartadelivery.review.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReviewUpdateRequest(
        @Schema(description = "평점")
        int rating,
        @Schema(description = "리뷰 내용")
        String content
) {
}
