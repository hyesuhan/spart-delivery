package com.sparta.spartadelivery.review.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.review.presentation.dto.ReviewDetailDto;
import com.sparta.spartadelivery.review.presentation.dto.ReviewUpdateRequest;
import com.sparta.spartadelivery.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
@RestController
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDetailDto>> getViewDetail(@PathVariable UUID reviewId) {
        ReviewDetailDto response = reviewService.viewDetail(reviewId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReview(@PathVariable UUID reviewId,
                                                          @RequestBody ReviewUpdateRequest request,
                                                          @AuthenticationPrincipal UserPrincipal customer) {
        Long customerId = customer.getId();
        reviewService.update(reviewId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }
}
