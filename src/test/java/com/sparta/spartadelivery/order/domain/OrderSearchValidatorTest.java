package com.sparta.spartadelivery.order.domain;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderRepository;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderValidateResult;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class OrderSearchValidatorTest {

    @InjectMocks
    private OrderSearchValidator validator;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private StoreRepository storeRepository;

    private final Long userId = 1L;
    private final UUID orderId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    @Nested
    @DisplayName("validOrderDetails 테스트")
    class ValidOrderDetails {

        @Test
        @DisplayName("성공 - CUSTOMER는 본인의 주문 상세를 조회할 수 있다.")
        void validOrderDetails_Customer_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            Order order = mock(Order.class);
            Address address = mock(Address.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(user.getRole()).willReturn(Role.CUSTOMER);
            given(order.getCustomerId()).willReturn(userId);
            given(order.getAddressId()).willReturn(addressId);
            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            OrderValidateResult result = validator.validOrderDetails(userId, orderId);

            // then
            assertThat(result.order()).isEqualTo(order);
            assertThat(result.address()).isEqualTo(address);
        }

        @Test
        @DisplayName("성공 - OWNER는 본인 상점의 주문 상세를 조회할 수 있다.")
        void validOrderDetails_Owner_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            Order order = mock(Order.class);
            Store store = mock(Store.class);
            Address address = mock(Address.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(user.getRole()).willReturn(Role.OWNER);
            given(storeRepository.findByOwner(user)).willReturn(store);
            given(store.getId()).willReturn(storeId);
            given(order.getStoreId()).willReturn(storeId);
            given(order.getAddressId()).willReturn(addressId);
            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            // when
            OrderValidateResult result = validator.validOrderDetails(userId, orderId);

            // then
            assertThat(result.order()).isEqualTo(order);
        }

        @Test
        @DisplayName("실패 - CUSTOMER가 타인의 주문에 접근하면 예외가 발생한다.")
        void validOrderDetails_Customer_Unauthorized() {
            // given
            UserEntity user = mock(UserEntity.class);
            Order order = mock(Order.class);
            Address address = mock(Address.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            given(order.getAddressId()).willReturn(addressId);
            given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

            given(user.getRole()).willReturn(Role.CUSTOMER);
            given(order.getCustomerId()).willReturn(999L); // 타인 ID

            // when & then
            assertThatThrownBy(() -> validator.validOrderDetails(userId, orderId))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS.getMessage());
        }
    }

    @Nested
    @DisplayName("validPageParameter 테스트")
    class ValidPageParameter {

        @ParameterizedTest
        @ValueSource(ints = {10, 30, 50})
        @DisplayName("성공 - 허용된 페이지 사이즈(10, 30, 50)는 통과한다.")
        void validPageSize_Success(int size) {
            // given
            Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt"));

            // when & then (예외가 발생하지 않아야 함)
            validator.validPageParameter(pageable);
        }

        @Test
        @DisplayName("실패 - 허용되지 않은 페이지 사이즈는 예외가 발생한다.")
        void validPageSize_Fail() {
            // given
            Pageable pageable = PageRequest.of(0, 20); // 20은 비허용

            // when & then
            assertThatThrownBy(() -> validator.validPageParameter(pageable))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.INVALID_PAGE_SIZE);
        }

        @Test
        @DisplayName("실패 - 허용되지 않은 정렬 필드(name)는 예외가 발생한다.")
        void validSort_Fail() {
            // given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

            // when & then
            assertThatThrownBy(() -> validator.validPageParameter(pageable))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.INVALID_SORT_QUERY);
        }
    }

    @Nested
    @DisplayName("validRoleUser 테스트")
    class ValidRoleUser {

        @Test
        @DisplayName("성공 - MASTER/MANAGER는 Role.MANAGER를 반환한다.")
        void validRoleUser_Admin_ReturnsManager() {
            // given
            UserEntity user = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.MASTER);

            // when
            Role result = validator.validRoleUser(userId, storeId);

            // then
            assertThat(result).isEqualTo(Role.MANAGER);
        }

        @Test
        @DisplayName("성공 - OWNER가 본인 상점 ID로 요청 시 Role.OWNER를 반환한다.")
        void validRoleUser_Owner_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            Store store = mock(Store.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.OWNER);
            given(storeRepository.findByOwner(user)).willReturn(store);
            given(store.getId()).willReturn(storeId);

            // when
            Role result = validator.validRoleUser(userId, storeId);

            // then
            assertThat(result).isEqualTo(Role.OWNER);
        }

        @Test
        @DisplayName("실패 - OWNER가 타인의 상점 ID로 요청 시 예외가 발생한다.")
        void validRoleUser_Owner_MismatchStore() {
            // given
            UserEntity user = mock(UserEntity.class);
            Store store = mock(Store.class);
            UUID mismatchId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.OWNER);
            given(storeRepository.findByOwner(user)).willReturn(store);
            given(store.getId()).willReturn(mismatchId); // 내 상점은 A인데

            // when & then (검색 시도 상점은 storeId)
            assertThatThrownBy(() -> validator.validRoleUser(userId, storeId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저 ID로 요청 시 USER_NOT_FOUND 예외가 발생한다.")
        void validRoleUser_UserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> validator.validRoleUser(userId, storeId))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(AuthErrorCode.USER_NOT_FOUND.getMessage());
        }
    }


}
