package com.sparta.spartadelivery.payment.domain.repository;


import com.sparta.spartadelivery.address.config.TestConfig;
import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentMethod;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("성공: 결제 정보를 저장하고 ID로 조회한다.")
    void save_and_findById_success() {
        // Given
        Payment payment = Payment.create(UUID.randomUUID(), PaymentMethod.CARD, 20000);

        // When
        Payment savedPayment = paymentRepository.save(payment);
        Optional<Payment> foundPayment = paymentRepository.findById(savedPayment.getId());

        // Then
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getAmount()).isEqualTo(20000);
        assertThat(foundPayment.get().getOrderId()).isEqualTo(payment.getOrderId());
    }

    @Test
    @DisplayName("성공: 여러 개의 주문 ID로 해당하는 결제 내역들을 조회한다.")
    void findAllByOrderIdIn_success() {
        // Given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID orderId3 = UUID.randomUUID();

        Payment payment1 = Payment.create(orderId1, PaymentMethod.CARD, 10000);
        Payment payment2 = Payment.create(orderId2, PaymentMethod.CARD, 20000);
        Payment payment3 = Payment.create(orderId3, PaymentMethod.CARD, 30000);

        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        // When
        List<Payment> results = paymentRepository.findAllByOrderIdIn(List.of(orderId1, orderId2));

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("orderId")
                .containsExactlyInAnyOrder(orderId1, orderId2);
    }

    @Test
    @DisplayName("성공: 조건에 맞는 데이터가 없는 경우 빈 리스트를 반환한다.")
    void findAllByOrderIdIn_empty_result() {
        // Given
        UUID targetOrderId = UUID.randomUUID();
        Payment payment = Payment.create(UUID.randomUUID(), PaymentMethod.CARD, 10000);
        paymentRepository.save(payment);

        // When
        List<Payment> results = paymentRepository.findAllByOrderIdIn(List.of(targetOrderId));

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("성공: 결제 상태를 업데이트한 후 DB에 반영되었는지 확인한다.")
    void update_status_reflection_check() {
        // Given
        Payment payment = Payment.create(UUID.randomUUID(), PaymentMethod.CARD, 15000);
        Payment saved = paymentRepository.save(payment);

        // When
        saved.updateStatus(PaymentStatus.CANCELLED);
        paymentRepository.saveAndFlush(saved); // 영속성 컨텍스트를 DB에 강제 반영

        // Then
        Payment updated = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }
}
