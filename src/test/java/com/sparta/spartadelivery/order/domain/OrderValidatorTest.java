package com.sparta.spartadelivery.order.domain;

import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderValidator 단위 테스트")
public class OrderValidatorTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private OrderValidator orderValidator;

    @Nested
    @DisplayName("주문 생성 검증 (validCreateOrder)")
    class CreateOrderValidation {

        @Test
        @DisplayName("가게가 존재하지 않으면 예외가 발생한다.")
        void validateStore_Fail() {
            // given
            UUID storeId = UUID.randomUUID();
            OrderCreateRequest request = new OrderCreateRequest(storeId, UUID.randomUUID(), OrderType.ONLINE, null, List.of());
            given(storeRepository.existsById(storeId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> orderValidator.validCreateOrder(1L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.STORE_NOT_AVAILABLE.getMessage());
        }

        @Test
        @DisplayName("메뉴의 가격이나 이름이 DB와 다르면 예외가 발생한다.")
        void validateMenuPrice_Mismatch() {
            // given
            UUID menuId = UUID.randomUUID();
            Long userId = 1L;
            UUID storeId = UUID.randomUUID();
            UUID addressId = UUID.randomUUID();

            // 1. DTO 생성 (단가 10,000원)
            OrderItemRequest itemRequest = new OrderItemRequest(menuId, "후라이드", 1, 10000);
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId,OrderType.ONLINE, null, List.of(itemRequest));

            // 2. Mock 설정
            given(storeRepository.existsById(storeId)).willReturn(true);
            given(addressRepository.existsByIdAndCustomerId(addressId, userId)).willReturn(true);

            // DB에는 가격이 12,000원으로 저장되어 있다고 가정
            Menu mockMenu = mock(Menu.class);
            given(mockMenu.getId()).willReturn(menuId);
            given(mockMenu.getPrice()).willReturn(12000);
            given(menuRepository.findAllById(List.of(menuId))).willReturn(List.of(mockMenu));

            // when & then
            assertThatThrownBy(() -> orderValidator.validCreateOrder(userId, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.MENU_PRICE_MISMATCH.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 수정 검증 (validUpdateRequest)")
    class UpdateRequestValidation {

        @Test
        @DisplayName("주문 상태가 PENDING이 아니면 수정할 수 없다.")
        void update_Fail_StatusNotPending() {
            // given
            Order order = mock(Order.class);
            given(order.getStatus()).willReturn(OrderStatus.CANCELED);

            // when & then
            assertThatThrownBy(() -> orderValidator.validUpdateRequest(1L, order))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.ORDER_CALCEL_ONLY_PENDING.getMessage());
        }

        @Test
        @DisplayName("주문자와 요청자가 다르면 수정할 수 없다.")
        void update_Fail_UserMismatch() {
            // given
            Order order = mock(Order.class);
            given(order.getStatus()).willReturn(OrderStatus.PENDING);
            given(order.getCustomerId()).willReturn(1L); // 주문자는 1번

            // when & then
            assertThatThrownBy(() -> orderValidator.validUpdateRequest(2L, order)) // 요청자는 2번
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 취소/삭제 권한 검증")
    class AuthorityValidation {

        @Test
        @DisplayName("MASTER 권한을 가진 사용자는 삭제 검증을 통과하고 이름을 반환한다.")
        void deleteByMaster_Success() {
            // given
            Long userId = 1L;
            UserEntity master = mock(UserEntity.class);
            given(master.getRole()).willReturn(Role.MASTER);
            given(master.getUsername()).willReturn("masterUser");
            given(userRepository.findById(userId)).willReturn(Optional.of(master));

            // when
            String username = orderValidator.validDeleteOrderByMaster(userId);

            // then
            assertThat(username).isEqualTo("masterUser");
        }

        @Test
        @DisplayName("CUSTOMER 권한이 MASTER 전용 삭제 메서드를 호출하면 예외가 발생한다.")
        void deleteByMaster_Fail_RoleCustomer() {
            // given
            Long userId = 1L;
            UserEntity customer = mock(UserEntity.class);
            given(customer.getRole()).willReturn(Role.CUSTOMER);
            given(userRepository.findById(userId)).willReturn(Optional.of(customer));

            // when & then
            assertThatThrownBy(() -> orderValidator.validDeleteOrderByMaster(userId))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS.getMessage());
        }
    }
}
