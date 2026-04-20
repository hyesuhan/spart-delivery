package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.address.config.TestConfig;
import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class OrderTest {

    private UserEntity user;
    private Address address;

    @BeforeEach
    void setUp() {
        user = mock(UserEntity.class);
        address = mock(Address.class);
    }

    @Test
    @DisplayName("주문 생성 시 기본 상태는 PENDING 입니다.")
    void order_DefaultStatus_isPending() {

        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        // when
        OrderStatus status = order.getStatus();

        // then
        assertThat(status).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 생성 시 기본 타입은 ONLINE 입니다.")
    void order_DefaultType_isOnline() {

        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        // when
        OrderType type = order.getType();

        // then
        assertThat(type).isEqualTo(OrderType.ONLINE);
    }

    @Test
    @DisplayName("주문 상태를 CANCELLED로 변경할 수 있다.(5분 이내)")
    void order_CancelledStatus_canBeSet() {

        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("주문 생성 후 5분이 지나면 취소할 수 없다")
    void order_Cancel_afterFiveMin_throwsException() {
        //given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();
        LocalDateTime sixMiAgo = LocalDateTime.now().minusMinutes(6);
        ReflectionTestUtils.setField(order, "createdAt", sixMiAgo);

        // when & then
        assertThrows(IllegalStateException.class, order::cancel);

    }

    @Test
    @DisplayName("주문생성 후 정확히 5분 이라면 취소할수 있다.")
    void order_Cancel_afterExactlyFiveMin_canBeCancelled() {
        //given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();
        LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(5);
        ReflectionTestUtils.setField(order, "createdAt", fiveMinAgo);

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("주문 생성 후 요청 사항을 수정할 수 있다.(PENDING에서만)")
    void order_Request_canBeUpdated() {
        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        // when
        order.updateRequest("문 앞에 놔주세요");

        // then
        assertThat(order.getRequest()).isEqualTo("문 앞에 놔주세요");
    }

    @Test
    @DisplayName("주문이 PENDING이 아닐 때 요청 사항을 수정하려고 하면 예외가 발생한다.")
    void order_Request_update_throwsException_whenNotPending() {
        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        LocalDateTime fiveMinAgo = LocalDateTime.now().minusMinutes(3);
        ReflectionTestUtils.setField(order, "createdAt", fiveMinAgo);

        order.cancel(); // 상태를 CANCELED로 변경

        // when & then
        assertThrows(IllegalStateException.class, () -> order.updateRequest("문 앞에 놔주세요"));
    }


    @Test
    @DisplayName("주문 내역을 삭제할 수 있다.")
    void canDelete_Order() {
        // 마스터가 가능함

        // given
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항 없음")
                .build();

        // when
        order.markDeleted("master");

        // then
        assertThat(order.isDeleted()).isTrue();
    }


    @Test
    @DisplayName("totalPrice가 음수일수는 없다.")
    void order_TotalPrice_cannotBeNegative() {
        // given & when & then
        assertThatThrownBy(() ->
                Order.builder()
                        .user(user)
                        .address(address)
                        .totalPrice(-10)
                        .request("요청사항")
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 총액은 음수일 수 없습니다.");

    }

    @Test
    @DisplayName("totalPrice가 양수라면 정상 생성")
    void order_TotalPrice_Success() {
        // given & when
        Order order = Order.builder()
                .user(user)
                .address(address)
                .totalPrice(10000)
                .request("요청사항")
                .build();

        // then
        assertThat(order.getTotalPrice()).isEqualTo(10000);
    }

}
