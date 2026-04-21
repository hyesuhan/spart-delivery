package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;

import java.util.List;
import java.util.UUID;

public interface OrderItemUsecase {

    // 주문 아이템 생성 및 저장 - snapshot 필요
    //void createOrderItems(UUID orderId, List<OrderItemRequest> itemRequests);

    // 특정 주문의 아이템 목록 조회
    //List<OrderItemResponse> getItemsByOrderId(UUID orderID);

    // 주문 삭제 시 아이템 soft delete
    //void deleteOrderItems(UUID orderId);


}
