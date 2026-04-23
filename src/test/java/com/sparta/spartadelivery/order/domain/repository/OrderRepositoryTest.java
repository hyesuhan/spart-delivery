package com.sparta.spartadelivery.order.domain.repository;

import com.sparta.spartadelivery.address.config.TestConfig;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("삭제되지 않은 주문은 ID로 조회되어야 한다.")
    void findByIdAndDeletedAtIsNull_Success() {
        // given
        Order order = createOrder();
        Order savedOrder = orderRepository.save(order);

        // when
        Optional<Order> found = orderRepository.findByIdAndDeletedAtIsNull(savedOrder.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedOrder.getId());
    }

    @Test
    @DisplayName("deletedAt이 설정된 주문은 조회되지 않아야 한다.")
    void findByIdAndDeletedAtIsNull_ReturnEmpty() {
        // given
        Order order = createOrder();
        ReflectionTestUtils.setField(order, "deletedAt", LocalDateTime.now()); // Soft Delete 상태 시뮬레이션
        Order savedOrder = orderRepository.save(order);

        // when
        Optional<Order> found = orderRepository.findByIdAndDeletedAtIsNull(savedOrder.getId());

        // then
        assertThat(found).isEmpty();
    }

    private Order createOrder() {
        return Order.create(
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(OrderItem.create(UUID.randomUUID(), "메뉴", 1, 10000)),
                "요청사항"
        );
    }

}
