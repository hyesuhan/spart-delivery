package com.sparta.spartadelivery.payment.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {

    @Nested
    @DisplayName("결제 객체 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("성공: 유효한 정보로 결제 객체를 생성하면 상태는 COMPLETED여야 한다.")
        void create_payment_success() {
            // Given
            UUID orderId = UUID.randomUUID();
            PaymentMethod method = PaymentMethod.CARD;
            Integer amount = 15000;

            // When
            Payment payment = Payment.create(orderId, method, amount);

            // Then
            assertThat(payment).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(orderId);
            assertThat(payment.getPaymentMethod()).isEqualTo(method);
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("결제 상태 수정 테스트")
    class UpdateStatusTest {

        @Test
        @DisplayName("성공: 결제 상태를 다른 상태로 변경할 수 있다.")
        void updateStatus_success() {
            // Given
            Payment payment = Payment.create(UUID.randomUUID(), PaymentMethod.CARD, 10000);
            PaymentStatus newStatus = PaymentStatus.CANCELLED;

            // When
            payment.updateStatus(newStatus);

            // Then
            assertThat(payment.getStatus()).isEqualTo(newStatus);
        }
    }

    @Nested
    @DisplayName("결제 삭제(논리 삭제) 테스트")
    class DeleteTest {

        @Test
        @DisplayName("성공: 삭제 시 BaseEntity의 삭제 로직이 호출되어야 한다.")
        void delete_success() {
            // Given
            Payment payment = Payment.create(UUID.randomUUID(), PaymentMethod.CARD, 10000);
            String deletedBy = "admin@sparta.com";

            // When
            payment.delete(deletedBy);

            // Then
            // BaseEntity에서 제공하는 필드 및 메서드 검증 (상속된 로직 가정)
            assertThat(payment.isDeleted()).isTrue();
            assertThat(payment.getDeletedBy()).isEqualTo(deletedBy);
            assertThat(payment.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("필드 데이터 정합성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("성공: 생성된 결제의 모든 필드가 입력값과 일치해야 한다.")
        void field_integrity_check() {
            // Given
            UUID orderId = UUID.randomUUID();
            PaymentMethod method = PaymentMethod.CARD;
            Integer amount = 50000;

            // When
            Payment payment = Payment.create(orderId, method, amount);

            // Then
            assertThat(payment).extracting("orderId", "paymentMethod", "amount", "status")
                    .containsExactly(orderId, method, amount, PaymentStatus.COMPLETED);
        }
    }
}
