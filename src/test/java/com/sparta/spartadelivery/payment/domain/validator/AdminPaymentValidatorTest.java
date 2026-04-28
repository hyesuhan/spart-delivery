package com.sparta.spartadelivery.payment.domain.validator;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AdminPaymentValidatorTest {

    @InjectMocks
    private AdminPaymentValidator adminPaymentValidator;

    @Mock
    private UserRepository userRepository;

    @Nested
    @DisplayName("isAdmin (MASTER/MANAGER) 권한 검증")
    class IsAdminTest {

        @Test
        @DisplayName("성공: MASTER 권한 유저는 예외 없이 통과한다.")
        void success_when_user_is_master() {
            // Given
            Long userId = 1L;
            UserEntity master = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(master));
            given(master.getRole()).willReturn(Role.MASTER);

            // When & Then
            adminPaymentValidator.isAdmin(userId);

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("성공: MANAGER 권한 유저는 예외 없이 통과한다.")
        void success_when_user_is_manager() {
            // Given
            Long userId = 1L;
            UserEntity manager = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(manager));
            given(manager.getRole()).willReturn(Role.MANAGER);

            // When & Then
            adminPaymentValidator.isAdmin(userId);

            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("실패: CUSTOMER 권한 유저는 NO_ACCESS_PERMISSION 예외가 발생한다.")
        void fail_when_user_is_customer() {
            // Given
            Long userId = 1L;
            UserEntity customer = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(customer));
            given(customer.getRole()).willReturn(Role.CUSTOMER);

            // When & Then
            assertThatThrownBy(() -> adminPaymentValidator.isAdmin(userId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.NO_ACCESS_PERMISSION);
        }
    }

    @Nested
    @DisplayName("isMaster 권한 검증")
    class IsMasterTest {

        @Test
        @DisplayName("성공: MASTER 권한 유저라면 유저네임을 반환한다.")
        void success_when_user_is_master_return_username() {
            // Given
            Long userId = 1L;
            String username = "master_user";
            UserEntity master = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(master));
            given(master.getRole()).willReturn(Role.MASTER);
            given(master.getUsername()).willReturn(username);

            // When
            String result = adminPaymentValidator.isMaster(userId);

            // Then
            assertThat(result).isEqualTo(username);
        }

        @Test
        @DisplayName("실패: MANAGER 권한 유저라도 isMaster에서는 NO_ACCESS_PERMISSION 예외가 발생한다.")
        void fail_when_user_is_manager() {
            // Given
            Long userId = 1L;
            UserEntity manager = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(manager));
            given(manager.getRole()).willReturn(Role.MANAGER);

            // When & Then
            assertThatThrownBy(() -> adminPaymentValidator.isMaster(userId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PayErrorCode.NO_ACCESS_PERMISSION);
        }
    }

    @Nested
    @DisplayName("공통 예외 상황")
    class CommonExceptionTest {

        @Test
        @DisplayName("실패: 존재하지 않는 유저 ID로 조회 시 USER_NOT_FOUND 예외가 발생한다.")
        void fail_when_user_not_found() {
            // Given
            Long userId = 999L;
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> adminPaymentValidator.isAdmin(userId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_NOT_FOUND);
        }
    }
}
