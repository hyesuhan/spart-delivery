package com.sparta.spartadelivery.order.presentation.dto.response;

import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order Search Response unit test (동적 쿼리용)")
public class OrderSearchResponseTest {

    @Test
    @DisplayName("레코드 생성 및 데이터 정합성 검증 (FIRST: Fast, Independent)")
    void createOrderSearchResponse_Success() {
        // given: 테스트에 필요한 데이터 준비
        UUID orderId = UUID.randomUUID();
        Long customerId = 1L;
        UUID storeId = UUID.randomUUID();
        String storeName = "스파르타 치킨";
        OrderStatus status = OrderStatus.DELIVERED; // OrderStatus가 Enum이라고 가정
        String firstItemName = "황금올리브 치킨";
        String request = "문 앞에 두고 벨 눌러주세요.";
        LocalDateTime createdAt = LocalDateTime.now();

        // when: 레코드 생성 (생성자 호출)
        OrderSearchResponse response = new OrderSearchResponse(
                orderId,
                customerId,
                storeId,
                storeName,
                status,
                firstItemName,
                request,
                createdAt
        );

        // then: AssertJ를 이용한 데이터 검증 (Self-Validating)
        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.storeId()).isEqualTo(storeId);
        assertThat(response.storeName()).isEqualTo(storeName);
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.firstItemName()).isEqualTo(firstItemName);
        assertThat(response.request()).isEqualTo(request);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Java Record의 equals 및 hashCode 동작 검증")
    void recordEqualsTest() {
        // given
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderSearchResponse res1 = new OrderSearchResponse(orderId, 1L, null, "A", null, "Item", "Req", now);
        OrderSearchResponse res2 = new OrderSearchResponse(orderId, 1L, null, "A", null, "Item", "Req", now);

        // when & then: 레코드는 모든 필드 값이 같으면 equals가 true여야 함
        assertThat(res1).isEqualTo(res2);
        assertThat(res1.hashCode()).isEqualTo(res2.hashCode());
    }

}
