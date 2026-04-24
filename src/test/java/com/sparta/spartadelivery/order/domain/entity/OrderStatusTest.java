package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderStatus 단위 테스트")
class OrderStatusTest {

    @ParameterizedTest(name = "{0} 상태에서 다음 단계는 {1}이다")
    @CsvSource({
            "PENDING, ACCEPTED",
            "ACCEPTED, PREPARING",
            "PREPARING, OUT_FOR_DELIVERY",
            "OUT_FOR_DELIVERY, DELIVERED"
    })
    @DisplayName("성공: 정의된 순서대로 다음 상태를 반환한다")
    void getNextStatus_Success(OrderStatus current, OrderStatus next) {
        // when
        OrderStatus result = current.getNextStatus();

        // then
        assertThat(result).isEqualTo(next);
    }

    @Test
    @DisplayName("실패: 이미 배달 완료된 상태에서 다음 단계로 이동 시 예외가 발생한다")
    void getNextStatus_AlreadyDelivered_Fail() {
        // given
        OrderStatus status = OrderStatus.DELIVERED;

        // when & then
        assertThatThrownBy(status::getNextStatus)
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ALREADY_DELIVERED);
    }

    @Test
    @DisplayName("실패: 취소된 주문 상태에서 다음 단계로 이동 시 예외가 발생한다")
    void getNextStatus_AlreadyCanceled_Fail() {
        // given
        OrderStatus status = OrderStatus.CANCELED;

        // when & then
        assertThatThrownBy(status::getNextStatus)
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_ALREADY_CANCLED);
    }
}
