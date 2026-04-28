package com.sparta.spartadelivery.payment.domain.validator;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UserPaymentValidatorTest {
    @InjectMocks
    private UserPaymentValidator userPaymentValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("결제 생성 권한 및 금액 검증")
    class ValidCreatePaymentAndGetAmount {

        @Test
        @DisplayName("성공: 주문 고객과 요청 유저가 일치하면 주문 금액을 반환한다.")
        void success_when_user_matches_order_customer() {
            // Given
            Long userId = 1L;
            UUID orderId = UUID.randomUUID();
            Order mockOrder = mock(Order.class);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));
            given(mockOrder.getCustomerId()).willReturn(userId);
            given(mockOrder.getTotalPrice()).willReturn(20000);

            // When
            Integer amount = userPaymentValidator.validCreatePaymentAndGetAmount(userId, orderId);

            // Then
            assertThat(amount).isEqualTo(20000);
        }

        @Test
        @DisplayName("실패: 주문 존재하지 않을 경우 ORDER_NOT_FOUND 예외가 발생한다.")
        void fail_when_order_not_found() {
            // Given
            UUID orderId = UUID.randomUUID();
            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userPaymentValidator.validCreatePaymentAndGetAmount(1L, orderId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 주문 고객과 요청 유저가 불일치하면 PAYMENT_NOT_FOUND 예외가 발생한다.")
        void fail_when_user_mismatch() {
            // Given
            Long userId = 1L;
            Long anotherUserId = 2L;
            UUID orderId = UUID.randomUUID();
            Order mockOrder = mock(Order.class);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(mockOrder));
            given(mockOrder.getCustomerId()).willReturn(anotherUserId);

            // When & Then
            assertThatThrownBy(() -> userPaymentValidator.validCreatePaymentAndGetAmount(userId, orderId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("어드민 권한 체크")
    class CheckAdminTest {

        @Test
        @DisplayName("성공: 유저 권한이 MASTER 혹은 MANAGER이면 true를 반환한다.")
        void success_admin_roles() {
            // Given
            Long userId = 1L;
            UserEntity master = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(master));
            given(master.getRole()).willReturn(Role.MASTER);

            // When
            boolean result = userPaymentValidator.checkAdmin(userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공: 유저 권한이 CUSTOMER이면 false를 반환한다.")
        void return_false_for_customer() {
            // Given
            Long userId = 1L;
            UserEntity customer = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(customer));
            given(customer.getRole()).willReturn(Role.CUSTOMER);

            // When
            boolean result = userPaymentValidator.checkAdmin(userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패: 유저를 찾을 수 없으면 USER_NOT_FOUND 예외가 발생한다.")
        void fail_when_user_not_found() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userPaymentValidator.checkAdmin(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("정보 조회 권한 검증")
    class IsValidGetInfoTest {

        @Test
        @DisplayName("성공: 유저가 MASTER/MANAGER인 경우 항상 true를 반환한다.")
        void success_for_admin_roles() {
            // Given
            UserEntity admin = mock(UserEntity.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(admin));
            given(admin.getRole()).willReturn(Role.MANAGER);

            // When
            boolean result = userPaymentValidator.isValidGetInfo(1L, "any-creator");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공: CUSTOMER 권한 유저의 이름이 생성자와 일치하면 true를 반환한다.")
        void success_for_customer_same_username() {
            // Given
            UserEntity customer = mock(UserEntity.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(customer));
            given(customer.getRole()).willReturn(Role.CUSTOMER);
            given(customer.getUsername()).willReturn("sparta-user");

            // When
            boolean result = userPaymentValidator.isValidGetInfo(1L, "sparta-user");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공: CUSTOMER 권한 유저의 이름이 생성자와 다르면 false를 반환한다.")
        void fail_for_customer_different_username() {
            // Given
            UserEntity customer = mock(UserEntity.class);
            given(userRepository.findById(1L)).willReturn(Optional.of(customer));
            given(customer.getRole()).willReturn(Role.CUSTOMER);
            given(customer.getUsername()).willReturn("sparta-user");

            // When
            boolean result = userPaymentValidator.isValidGetInfo(1L, "other-user");

            // Then
            assertThat(result).isFalse();
        }
    }
}
