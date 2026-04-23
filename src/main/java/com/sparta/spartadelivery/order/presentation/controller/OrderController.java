package com.sparta.spartadelivery.order.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.order.application.OrderService;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.UpdateOrderRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "주문 생성 API",
            description = """
                    CUSTOMER 만 주문이 가능합니다.
                    
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody OrderCreateRequest request
            ) {
        OrderResponse response = orderService.createOrder(userPrincipal.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));

    }

    @Operation(
            summary = "주문 요청 사항 수정 API",
            description = """
                    CUSTOMER 만 주문 요청 사항 수정이 가능합니다.
                    
                    - 단, PENDING 상태일 때만 수정이 가능합니다.
                    """
    )
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> updateOrderRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderRequest request
            ) {

        orderService.updateOrderRequest(userPrincipal.getId(), orderId, request.message());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));

    }

    @Operation(
            summary = "주문 취소 API",
            description = """
                    CUSTOMER: 5분 이내에만 주문 취소가 가능합니다. (서버 시간 기준)
                    
                    MASTER: 5분 이내에만 주문 취소가 가능합니다.  (서버 시간 기준)                  
                    """
    )
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId

    ) {

        orderService.cancelOrder(userPrincipal.getId(), orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", null));
    }

    @Operation(
            summary = "주문 삭제 API",
            description = """
                    MASTER: 특정 주문을 삭제할 수 있습니다. (softdelete)
                    """
    )
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> deleteOrder (
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId
    ) {
        orderService.deleteOrder(userPrincipal.getId(), orderId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT.value(), "DELETED", null));
    }

}
