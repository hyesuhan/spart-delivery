package com.sparta.spartadelivery.payment.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.payment.application.PaymentService;
import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentMethod;
import com.sparta.spartadelivery.payment.domain.repository.PaymentRepository;
import com.sparta.spartadelivery.payment.domain.validator.UserPaymentValidator;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentRequest;
import com.sparta.spartadelivery.payment.presentation.dto.response.PaymentDetailResponse;
import com.sparta.spartadelivery.payment.presentation.dto.response.PaymentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserPaymentValidator validator;

    @Mock
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("결제 프로세스 진행 (processPayment)")
    class ProcessPaymentTest {

        @Test
        @DisplayName("성공: 유효한 요청일 경우 결제를 생성하고 저장한다.")
        void processPayment_success() {
            // Given
            Long userId = 1L;
            UUID orderId = UUID.randomUUID();
            PaymentRequest request = new PaymentRequest(PaymentMethod.CARD);
            Integer amount = 20000;

            given(validator.validCreatePaymentAndGetAmount(userId, orderId)).willReturn(amount);

            // When
            PaymentResponse response = paymentService.processPayment(userId, orderId, request);

            // Then
            assertThat(response).isNotNull();
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("전체 결제 내역 조회 (getAllPayments)")
    class GetAllPaymentsTest {

        @Test
        @DisplayName("성공: 관리자인 경우 모든 결제 내역을 최신순으로 반환한다.")
        void success_when_admin_gets_all() {
            // Given
            Long adminId = 1L;
            given(validator.checkAdmin(adminId)).willReturn(true);

            Payment payment = mock(Payment.class);
            given(paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")))
                    .willReturn(List.of(payment));

            // When
            List<PaymentResponse> results = paymentService.getAllPayments(adminId);

            // Then
            assertThat(results).hasSize(1);
            verify(paymentRepository).findAll(any(Sort.class));
            verify(orderRepository, never()).findAllByCustomerId(any());
        }

        @Test
        @DisplayName("성공: 일반 고객인 경우 본인의 주문에 연결된 결제 내역만 반환한다.")
        void success_when_customer_gets_own_payments() {
            // Given
            Long userId = 2L;
            UUID orderId = UUID.randomUUID();
            Order mockOrder = mock(Order.class);
            Payment mockPayment = mock(Payment.class);

            given(validator.checkAdmin(userId)).willReturn(false);
            given(orderRepository.findAllByCustomerId(userId)).willReturn(List.of(mockOrder));
            given(mockOrder.getId()).willReturn(orderId);
            given(paymentRepository.findAllByOrderIdIn(List.of(orderId))).willReturn(List.of(mockPayment));

            // When
            List<PaymentResponse> results = paymentService.getAllPayments(userId);

            // Then
            assertThat(results).hasSize(1);
            verify(paymentRepository).findAllByOrderIdIn(anyList());
        }

        @Test
        @DisplayName("성공: 주문 내역이 없는 고객은 빈 리스트를 반환한다.")
        void return_empty_when_no_orders() {
            // Given
            Long userId = 2L;
            given(validator.checkAdmin(userId)).willReturn(false);
            given(orderRepository.findAllByCustomerId(userId)).willReturn(List.of());

            // When
            List<PaymentResponse> results = paymentService.getAllPayments(userId);

            // Then
            assertThat(results).isEmpty();
            verify(paymentRepository, never()).findAllByOrderIdIn(anyList());
        }
    }

    @Nested
    @DisplayName("결제 상세 조회 (getDetailPayment)")
    class GetDetailPaymentTest {

        @Test
        @DisplayName("성공: 결제가 존재하고 조회 권한이 있으면 상세 정보를 반환한다.")
        void success_get_detail_with_permission() {
            // Given
            Long userId = 1L;
            UUID paymentId = UUID.randomUUID();
            Payment mockPayment = mock(Payment.class);
            String creator = "user1";

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(mockPayment));
            given(mockPayment.getCreatedBy()).willReturn(creator);
            given(validator.isValidGetInfo(userId, creator)).willReturn(true);

            // When
            PaymentDetailResponse response = paymentService.getDetailPayment(userId, paymentId);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("실패: 결제 정보가 존재하지 않으면 PAYMENT_NOT_FOUND 예외가 발생한다.")
        void fail_when_payment_not_found() {
            // Given
            UUID paymentId = UUID.randomUUID();
            given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentService.getDetailPayment(1L, paymentId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 조회 권한이 없으면 NO_ACCESS_PERMISSION 예외가 발생한다.")
        void fail_when_no_permission() {
            // Given
            Long userId = 1L;
            UUID paymentId = UUID.randomUUID();
            Payment mockPayment = mock(Payment.class);
            String creator = "otherUser";

            given(paymentRepository.findById(paymentId)).willReturn(Optional.of(mockPayment));
            given(mockPayment.getCreatedBy()).willReturn(creator);
            given(validator.isValidGetInfo(userId, creator)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> paymentService.getDetailPayment(userId, paymentId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.NO_ACCESS_PERMISSION);
        }
    }
}
