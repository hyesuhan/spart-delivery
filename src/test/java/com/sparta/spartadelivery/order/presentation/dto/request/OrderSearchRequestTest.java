package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.user.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderSearch Request & Condition unit test")
public class OrderSearchRequestTest {

    @Test
    @DisplayName("customer condition create test - 본인 userId와 요청 storeId 포함")
    void customerCondition_Success() {
        // given
        Long userId = 100L;
        UUID storeId = UUID.randomUUID();
        OrderSearchRequest request = new OrderSearchRequest(storeId, OrderStatus.PENDING);

        // when
        OrderSearchRequest.SearchCondition condition = OrderSearchRequest.SearchCondition.customerCondition(userId, request);

        // then
        assertThat(condition.userRole()).isEqualTo(Role.CUSTOMER);
        assertThat(condition.userId()).isEqualTo(userId);
        assertThat(condition.storeId()).isEqualTo(storeId);
        assertThat(condition.orderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(condition.ownedStoreId()).isNull(); // 고객은 본인 소유 가게 ID가 없음
    }

    @Test
    @DisplayName("사장님(OWNER) 조건 생성 테스트 - storeId 필터는 무시되고 본인의 ownedStoreId가 설정되어야 한다.")
    void ownerCondition_Success() {
        // given
        Long userId = 200L;
        UUID requestedStoreId = UUID.randomUUID(); // 검색 필터로 들어온 ID
        UUID ownedStoreId = UUID.randomUUID();    // 실제 사장님 소유 가게 ID
        OrderSearchRequest request = new OrderSearchRequest(requestedStoreId, OrderStatus.PREPARING);

        // when
        OrderSearchRequest.SearchCondition condition = OrderSearchRequest.SearchCondition.ownerCondition(userId, request, ownedStoreId);

        // then
        assertThat(condition.userRole()).isEqualTo(Role.OWNER);
        assertThat(condition.userId()).isEqualTo(userId);
        assertThat(condition.storeId()).isNull(); // 사장님은 본인 가게만 보므로 일반 storeId 필터는 null 처리
        assertThat(condition.orderStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(condition.ownedStoreId()).isEqualTo(ownedStoreId);
    }

    @Test
    @DisplayName("관리자(MANAGER) 조건 생성 테스트 - 모든 필터링 조건을 그대로 수용해야 한다.")
    void adminCondition_Success() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        OrderSearchRequest request = new OrderSearchRequest(storeId, OrderStatus.CANCELED);

        // when
        OrderSearchRequest.SearchCondition condition = OrderSearchRequest.SearchCondition.adminCondition(userId, request);

        // then
        assertThat(condition.userRole()).isEqualTo(Role.MANAGER);
        assertThat(condition.userId()).isEqualTo(userId);
        assertThat(condition.storeId()).isEqualTo(storeId);
        assertThat(condition.orderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(condition.ownedStoreId()).isNull();
    }
}
