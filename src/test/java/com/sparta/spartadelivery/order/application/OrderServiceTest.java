package com.sparta.spartadelivery.order.application;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderValidator;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderValidator orderValidator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {
        @Test
        @DisplayName("성공: 유효한 요청일 경우 주문이 생성되고 저장된다.")
        void createOrder_Success() {
            // given
            Long customerId = 1L;
            UUID storeId = UUID.randomUUID();
            UUID addressId = UUID.randomUUID();
            OrderItemRequest itemRequest = new OrderItemRequest(UUID.randomUUID(), "치킨", 1, 20000);
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId, OrderType.ONLINE, "빨리요", List.of(itemRequest));

            // validator는 아무 예외도 던지지 않음 (성공 가정)
            willDoNothing().given(orderValidator).validCreateOrder(customerId, request);

            // when
            OrderResponse response = orderService.createOrder(customerId, request);

            // then
            assertThat(response).isNotNull();
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(orderValidator, times(1)).validCreateOrder(customerId, request);
        }
    }

    @Nested
    @DisplayName("주문 요청사항 수정")
    class UpdateOrderRequest {
        @Test
        @DisplayName("성공: 주문이 존재하고 검증을 통과하면 요청사항이 변경된다.")
        void updateOrderRequest_Success() {
            // given
            UUID orderId = UUID.randomUUID();
            Long customerId = 1L;
            String newRequest = "문 앞에 놔주세요";
            Order order = mock(Order.class); // 내부 상태 변경 확인을 위해 Mock 사용 가능

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            willDoNothing().given(orderValidator).validUpdateRequest(customerId, order);

            // when
            orderService.updateOrderRequest(customerId, orderId, newRequest);

            // then
            verify(order, times(1)).updateRequest(newRequest);
            verify(orderValidator, times(1)).validUpdateRequest(customerId, order);
        }

        @Test
        @DisplayName("실패: 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
        void updateOrderRequest_Fail_NotFound() {
            // given
            UUID orderId = UUID.randomUUID();
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderRequest(1L, orderId, "update"))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {
        @Test
        @DisplayName("성공: 주문 취소 시 validator를 호출하고 엔티티의 cancel 로직을 실행한다.")
        void cancelOrder_Success() {
            // given
            UUID orderId = UUID.randomUUID();
            Long customerId = 1L;
            Order order = mock(Order.class);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when
            orderService.cancelOrder(customerId, orderId);

            // then
            verify(orderValidator, times(1)).validCancelOrder(customerId, order);
            verify(order, times(1)).cancel(any()); // LocalDateTime.now()가 전달됨
        }
    }

    @Nested
    @DisplayName("주문 삭제 (Master)")
    class DeleteOrder {
        @Test
        @DisplayName("성공: 마스터 권한 확인 후 Soft Delete 처리를 수행한다.")
        void deleteOrder_Success() {
            // given
            UUID orderId = UUID.randomUUID();
            Long masterId = 99L;
            Order order = mock(Order.class);
            String masterName = "Admin";

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderValidator.validDeleteOrderByMaster(masterId)).willReturn(masterName);

            // when
            orderService.deleteOrder(masterId, orderId);

            // then
            verify(orderValidator, times(1)).validDeleteOrderByMaster(masterId);
            verify(order, times(1)).markDeleted(masterName);
        }
    }


    @Test
    @DisplayName("실패: 주문 생성 후 5분이 지난 시점에 취소 시 ORDER_CANCEL_TIMEOUT 예외가 발생한다.")
    void cancelOrder_Fail_Timeout() {
        // given
        // 1. 주문 생성
        Order order = Order.create(1L, UUID.randomUUID(), UUID.randomUUID(),
                List.of(OrderItem.create(UUID.randomUUID(), "치킨", 1, 20000)), "request");

        // 2. 생성 시간을 6분 전으로 강제 설정 (Reflection 사용)
        LocalDateTime sixMinutesAgo = LocalDateTime.now().minusMinutes(6);
        org.springframework.test.util.ReflectionTestUtils.setField(order, "createdAt", sixMinutesAgo);

        // 3. 현재 시간 기준으로 취소 시도
        LocalDateTime now = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> order.cancel(now))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_CANCEL_TIMEOUT.getMessage());
    }
}
