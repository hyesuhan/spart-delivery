package com.sparta.spartadelivery.order.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.order.application.GetOrderService;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderDetailInfo;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @Operation(
            summary = "주문 상세 조회 API",
            description = """
                    CUSTOMER 본인의 주문만 조회가 가능하다.
                    OWNER 본인 가게의 주문만 조회가 가능하다.
                    MASTER/MANAGER 전체 허용
                    """
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailInfo>> getOrderDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId
            ) {
        OrderDetailInfo info = orderService.getOrderById(userPrincipal.getId(), orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", info));
    }

    @Operation(
            summary = "주문 조회 API",
            description = """
                    Request Param: 
                        - (선택) storeId - 특정 식당의 주문을 필터링
                        - (선택) orderstatus - 특정 주문 상태를 필터링
                    CUSTOMER: 본인의 전체 주문 내역 중 필터링을 합니다.
                    OWNER: 본인 영업장의 전체 주문 내역 중 필터링을 합니다.
                    MASTER/MANAGER: 전체 주문 내역 중 필터링을 합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSearchResponse>>> getOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
            ) {
        Page<OrderSearchResponse> response = orderService.search(userPrincipal.getId(), new OrderSearchRequest(storeId, orderStatus), pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", response));
    }

}
