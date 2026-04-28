package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.user.domain.entity.Role;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderSearchRequest(
        UUID storeId,
        OrderStatus orderStatus
) {

    public record SearchCondition(
            @NotNull
            Role userRole,
            @NotNull
            Long userId,
            UUID storeId, // 필터링
            OrderStatus orderStatus, // 필터링
            UUID ownedStoreId // 사장님 -> 가게 아이디 필요

    ) {
        public static SearchCondition customerCondition(Long userId, OrderSearchRequest request) {
            return new SearchCondition(Role.CUSTOMER, userId, request.storeId, request.orderStatus, null);
        }

        public static SearchCondition ownerCondition(Long userId, OrderSearchRequest request, UUID ownedStoreId) {
            return new SearchCondition(Role.OWNER, userId, null, request.orderStatus, ownedStoreId);
        }

        // 이는 MASTER 하위의 MANAGER 로 설정(임시)
        public static SearchCondition adminCondition(Long userId, OrderSearchRequest request) {
            return new SearchCondition(Role.MANAGER, userId, request.storeId, request.orderStatus, null);
        }
    }
}
