package com.sparta.spartadelivery.order.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.order.application.GetOrderService;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderDetailInfo;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderSearchController {

    private final GetOrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailInfo>> getOrderDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId
            ) {
        OrderDetailInfo info = orderService.getOrderById(userPrincipal.getId(), orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", info));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSearchResponse>>> getOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody OrderSearchRequest orderSearchRequest,
            Pageable pageable
            ) {
        Page<OrderSearchResponse> response = orderService.search(userPrincipal.getId(), orderSearchRequest, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", response));
    }

}
