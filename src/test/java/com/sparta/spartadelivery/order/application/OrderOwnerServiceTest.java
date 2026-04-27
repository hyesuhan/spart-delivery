package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderValidator;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderOwnerService 단위 테스트")
class OrderOwnerServiceTest {

    @InjectMocks
    private OrderOwnerService orderOwnerService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderValidator orderValidator;

    private final Long userId = 1L;
    private final UUID orderId = UUID.randomUUID();

    @Test
    @DisplayName("성공: 주문 상태를 다음 단계로 변경한다")
    void updateOrderStatus_Success() {
        // given
        Order order = mock(Order.class); // 상태 변경 메서드 호출 확인을 위해 mock 사용
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // orderValidator.validUpdateOrderStatus는 void이므로 별도 stubbing 불필요 (기본적으로 통과)

        // when
        orderOwnerService.updateOrderStatus(userId, orderId);

        // then
        verify(orderRepository).findById(orderId);
        verify(orderValidator).validUpdateOrderStatus(userId, order);
        verify(order).updateOrderStatus(); // 엔티티의 상태 변경 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("실패: 존재하지 않는 주문일 경우 ORDER_NOT_FOUND 예외가 발생한다")
    void updateOrderStatus_OrderNotFound() {
        // given
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderOwnerService.updateOrderStatus(userId, orderId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);

        // 검증: Repository 이후의 로직(Validator, Entity 메서드)은 실행되지 않아야 함
        verify(orderValidator, never()).validUpdateOrderStatus(anyLong(), any());
    }

    @Test
    @DisplayName("실패: 권한 검증(Validator)에서 예외가 발생하면 중단된다")
    void updateOrderStatus_ValidationFail() {
        // given
        Order order = mock(Order.class);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // Validator에서 예외를 던지도록 설정
        doThrow(new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS))
                .when(orderValidator).validUpdateOrderStatus(userId, order);

        // when & then
        assertThatThrownBy(() -> orderOwnerService.updateOrderStatus(userId, orderId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);

        // 검증: Entity의 상태 변경 메서드는 호출되지 않아야 함
        verify(order, never()).updateOrderStatus();
    }

    @Test
    @DisplayName("실패: 이미 배달 완료된 주문은 상태를 변경할 수 없다 (엔티티 로직 전파)")
    void updateOrderStatus_AlreadyDelivered() {
        // given
        Order order = mock(Order.class);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // Validator는 통과하지만, 엔티티 내부에서 예외가 발생하는 경우 상정
        doThrow(new AppException(OrderErrorCode.ALREADY_DELIVERED))
                .when(order).updateOrderStatus();

        // when & then
        assertThatThrownBy(() -> orderOwnerService.updateOrderStatus(userId, orderId))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ALREADY_DELIVERED);
    }
}