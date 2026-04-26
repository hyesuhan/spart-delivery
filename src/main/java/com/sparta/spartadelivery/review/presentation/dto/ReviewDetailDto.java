package com.sparta.spartadelivery.review.presentation.dto;

import com.sparta.spartadelivery.review.domain.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ReviewDetailDto(
        @Schema(description = "리뷰 id")
        UUID reviewId,
        @Schema(description = "주문 id")
        UUID orderId,
        @Schema(description = "고객 id")
        Long userId,
        @Schema(description = "리뷰 점수")
        int rating,
        @Schema(description = "리뷰 내용")
        String content
) {
    public static ReviewDetailDto from(Review review) {
        return new ReviewDetailDto(
                review.getId(),
                review.getOrder().getId(),
                review.getCustomer().getId(),
                review.getRating(),
                review.getContent()

        );
    }
}
