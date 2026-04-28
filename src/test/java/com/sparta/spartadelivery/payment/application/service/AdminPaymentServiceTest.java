package com.sparta.spartadelivery.payment.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.payment.application.AdminPaymentService;
import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;
import com.sparta.spartadelivery.payment.domain.repository.PaymentRepository;
import com.sparta.spartadelivery.payment.domain.validator.AdminPaymentValidator;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceTest {

    @InjectMocks
    private AdminPaymentService adminPaymentService;

    @Mock
    private AdminPaymentValidator validator;

    @Mock
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("결제 상태 수정 (updatePaymentStatus)")
    class UpdatePaymentStatusTest {

        @Test
        @DisplayName("성공: 관리자 권한이 있고 결제가 존재하면 상태를 변경한다.")
        void updateStatus_success() {
            // Given
            Long userId = 1L;
            UUID paymentId = UUID.randomUUID();
            PaymentStatus newStatus = PaymentStatus.CANCELLED;
            Payment mockPayment = mock(Payment.class);

            // validator.isAdmin(userId)는 void이므로 별도 stubbing 불필요 (예외 안 던지면 통과)
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(mockPayment));

            // When
            adminPaymentService.updatePaymentStatus(userId, paymentId, newStatus);

            // Then
            verify(validator).isAdmin(userId);
            verify(mockPayment).updateStatus(newStatus);
        }

        @Test
        @DisplayName("실패: 관리자 권한이 없으면 예외가 발생한다.")
        void updateStatus_fail_no_admin() {
            // Given
            Long userId = 1L;
            willThrow(new AppException(PayErrorCode.NO_ACCESS_PERMISSION))
                    .given(validator).isAdmin(userId);

            // When & Then
            assertThatThrownBy(() -> adminPaymentService.updatePaymentStatus(userId, UUID.randomUUID(), PaymentStatus.CANCELLED))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.NO_ACCESS_PERMISSION);
        }

        @Test
        @DisplayName("실패: 결제 정보가 존재하지 않으면 PAYMENT_NOT_FOUND 예외가 발생한다.")
        void updateStatus_fail_payment_not_found() {
            // Given
            UUID paymentId = UUID.randomUUID();
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminPaymentService.updatePaymentStatus(1L, paymentId, PaymentStatus.CANCELLED))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("결제 삭제 (deletePayment)")
    class DeletePaymentTest {

        @Test
        @DisplayName("성공: 마스터 권한이 있으면 논리 삭제를 진행하고 상태를 CANCELLED로 변경한다.")
        void delete_success() {
            // Given
            Long userId = 1L;
            UUID paymentId = UUID.randomUUID();
            String masterName = "master_admin";
            Payment mockPayment = mock(Payment.class);

            given(validator.isMaster(userId)).willReturn(masterName);
            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(mockPayment));

            // When
            adminPaymentService.deletePayment(userId, paymentId);

            // Then
            verify(validator).isMaster(userId);
            verify(mockPayment).delete(masterName);
            verify(mockPayment).updateStatus(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패: 마스터 권한이 아니면(isMaster 실패) 삭제할 수 없다.")
        void delete_fail_not_master() {
            // Given
            Long userId = 1L;
            given(validator.isMaster(userId)).willThrow(new AppException(PayErrorCode.NO_ACCESS_PERMISSION));

            // When & Then
            assertThatThrownBy(() -> adminPaymentService.deletePayment(userId, UUID.randomUUID()))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.NO_ACCESS_PERMISSION);

            verify(paymentRepository, never()).findById(any());
        }
    }
}
