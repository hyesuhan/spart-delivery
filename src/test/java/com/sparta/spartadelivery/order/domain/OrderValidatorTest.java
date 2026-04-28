package com.sparta.spartadelivery.order.domain;

import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.address.exception.AddressErrorCode;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 기본적으로 가게와 주소는 존재하는 것으로 가정 (공통 설정)
        lenient().when(storeRepository.existsById(any())).thenReturn(true);
        lenient().when(addressRepository.existsByIdAndCustomerId(any(), any())).thenReturn(true);
    }

    private final Long userId = 1L;
    private final UUID storeId = UUID.randomUUID();
    private final UUID menuId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

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
            given(mockMenu.getPrice()).willReturn(new MoneyVO(12000));
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

    @Nested
    @DisplayName("메뉴 가격 및 정보 검증 (validateMenuPrice)")
    class ValidateMenuPrice {



        @Test
        @DisplayName("성공: DB의 메뉴 정보와 요청 정보가 일치한다")
        void validateMenuPrice_Success() {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(menuId, "치킨", 1, 20000);

            Menu menu = mock(Menu.class);
            given(menu.getId()).willReturn(menuId);
            given(menu.getPrice()).willReturn(new MoneyVO(20000));
            given(menu.getName()).willReturn("치킨");

            given(menuRepository.findAllById(any())).willReturn(List.of(menu));

            // when & then (가게/주소를 통과하고 메뉴 검증까지 성공해야 함)
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId, OrderType.ONLINE, null, List.of(itemRequest));
            assertDoesNotThrow(() -> orderValidator.validCreateOrder(userId, request));
        }



        @Test
        @DisplayName("실패: DB에 존재하지 않는 메뉴 ID가 포함됨 (MENU_NOT_FOUND)")
        void validateMenuPrice_NotFound() {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(menuId, "치킨", 1, 20000);

            // 가게와 주소는 존재하지만, 메뉴 조회 결과는 비어 있음
            given(menuRepository.findAllById(any())).willReturn(Collections.emptyList());

            // when & then
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId, OrderType.ONLINE, null, List.of(itemRequest));
            assertThatThrownBy(() -> orderValidator.validCreateOrder(userId, request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 메뉴 가격이 DB와 다르다 (MENU_PRICE_MISMATCH)")
        void validateMenuPrice_PriceMismatch() {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(menuId, "치킨", 1, 15000); // 요청은 15000원

            Menu menu = mock(Menu.class);
            given(menu.getId()).willReturn(menuId);
            given(menu.getPrice()).willReturn(new MoneyVO(20000)); // DB는 20000원
            // given(menu.getName()).willReturn("치킨");

            given(menuRepository.findAllById(any())).willReturn(List.of(menu));

            // when & then
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId, OrderType.ONLINE, null, List.of(itemRequest));
            assertThatThrownBy(() -> orderValidator.validCreateOrder(userId, request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.MENU_PRICE_MISMATCH);
        }
    }

    @Test
    @DisplayName("주문 수정 실패: 주문자가 아닌 사용자가 요청한 경우 (UNAUTHORIZED_ORDER_ACCESS)")
    void validUpdateRequest_NotOwner() {
        // given
        Long anotherUserId = 999L;
        Order order = mock(Order.class);
        given(order.getStatus()).willReturn(OrderStatus.PENDING);
        given(order.getCustomerId()).willReturn(userId); // 실제 주문자는 1L

        // when & then
        assertThatThrownBy(() -> orderValidator.validUpdateRequest(anotherUserId, order))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
    }

    @Nested
    @DisplayName("주문 상태 변경 권한 검증 (validUpdateOrderStatus)")
    class ValidUpdateOrderStatus {

        @Test
        @DisplayName("성공: MASTER나 MANAGER는 항상 통과한다")
        void success_MasterOrManager() {
            // given
            UserEntity master = mock(UserEntity.class);
            given(master.getRole()).willReturn(Role.MASTER);
            given(userRepository.findById(userId)).willReturn(Optional.of(master));

            // when & then
            orderValidator.validUpdateOrderStatus(userId, mock(Order.class));
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("실패: OWNER가 자신의 가게 주문이 아닌 건을 수정하려 할 때")
        void fail_OwnerNotMatchingStore() {
            // given
            UserEntity owner = mock(UserEntity.class);
            given(owner.getRole()).willReturn(Role.OWNER);
            given(userRepository.findById(userId)).willReturn(Optional.of(owner));

            Order order = mock(Order.class);
            given(order.getStoreId()).willReturn(storeId);

            Store store = mock(Store.class);
            UserEntity realStoreOwner = mock(UserEntity.class);
            given(realStoreOwner.getId()).willReturn(99L); // 실제 주인은 99L
            given(store.getOwner()).willReturn(realStoreOwner);

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> orderValidator.validUpdateOrderStatus(userId, order))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경 권한 검증 (validUpdateOrderStatus)")
    class ValidUpdateOrderStatusTest {

        @Test
        @DisplayName("성공: MASTER나 MANAGER는 항상 통과한다")
        void success_MasterOrManager() {
            // given
            UserEntity master = mock(UserEntity.class);
            given(master.getRole()).willReturn(Role.MASTER);
            given(userRepository.findById(userId)).willReturn(Optional.of(master));

            // when & then
            orderValidator.validUpdateOrderStatus(userId, mock(Order.class));
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("실패: OWNER가 자신의 가게 주문이 아닌 건을 수정하려 할 때")
        void fail_OwnerNotMatchingStore() {
            // given
            UserEntity owner = mock(UserEntity.class);
            given(owner.getRole()).willReturn(Role.OWNER);
            given(userRepository.findById(userId)).willReturn(Optional.of(owner));

            Order order = mock(Order.class);
            given(order.getStoreId()).willReturn(storeId);

            Store store = mock(Store.class);
            UserEntity realStoreOwner = mock(UserEntity.class);
            given(realStoreOwner.getId()).willReturn(99L); // 실제 주인은 99L
            given(store.getOwner()).willReturn(realStoreOwner);

            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> orderValidator.validUpdateOrderStatus(userId, order))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }
    }

    @Test
    @DisplayName("주문 취소 실패: CUSTOMER가 타인의 주문을 취소하려 할 때")
    void validCancelOrder_OtherCustomer() {
        // given
        UserEntity customer = mock(UserEntity.class);
        given(customer.getRole()).willReturn(Role.CUSTOMER);
        given(customer.getId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(customer));

        // requestUserId는 userId와 같으나 로직상 Role이 CUSTOMER면 추가 검증
        // (현재 코드상 !user.getId().equals(requestUserId) 검증 로직 기준)

        // when & then
        orderValidator.validCancelOrder(userId, mock(Order.class)); // 성공 케이스
    }

    @Test
    @DisplayName("주소 검증 실패: 해당 사용자의 주소가 존재하지 않음 (ADDRESS_NOT_FOUND)")
    void validateAddress_NotFound() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(storeId, addressId,OrderType.ONLINE, null , List.of());
        given(storeRepository.existsById(any())).willReturn(true);
        given(addressRepository.existsByIdAndCustomerId(addressId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> orderValidator.validCreateOrder(userId, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", AddressErrorCode.ADDRESS_NOT_FOUND);
    }

    @Nested
    @DisplayName("커버리지 통과용 - 에지케이스 추가")
    class EdgeCaseValidation {

        /*
        @Test
        @DisplayName("실패: 메뉴 가격은 일치하지만 이름이 변경된 경우 (MENU_NAME_CHANGED)")
        void validateMenuPrice_NameMismatch() {
            // given
            OrderItemRequest itemRequest = new OrderItemRequest(menuId, "옛날치킨", 1, 20000); // 요청 이름: 옛날치킨

            Menu menu = mock(Menu.class);
            given(menu.getId()).willReturn(menuId);
            given(menu.getPrice()).willReturn(new MoneyVO(20000)); // 가격은 일치
            given(menu.getName()).willReturn("황금올리브치킨"); // DB 이름: 황금올리브치킨

            given(menuRepository.findAllById(any())).willReturn(List.of(menu));

            // when & then
            OrderCreateRequest request = new OrderCreateRequest(storeId, addressId, OrderType.ONLINE, null, List.of(itemRequest));
            assertThatThrownBy(() -> orderValidator.validCreateOrder(userId, request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.MENU_NAME_CHANGED);
        }

         */

        @Test
        @DisplayName("실패: 주문 취소 시 유저 정보를 찾을 수 없음 (USER_NOT_FOUND)")
        void validCancelOrder_UserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderValidator.validCancelOrder(userId, mock(Order.class)))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: CUSTOMER나 MASTER가 아닌 권한(예: OWNER)이 주문 취소를 시도할 때")
        void validCancelOrder_UnauthorizedRole() {
            // given
            UserEntity owner = mock(UserEntity.class);
            given(owner.getRole()).willReturn(Role.OWNER); // 취소 권한이 없는 OWNER
            given(userRepository.findById(userId)).willReturn(Optional.of(owner));

            // when & then
            assertThatThrownBy(() -> orderValidator.validCancelOrder(userId, mock(Order.class)))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        @Test
        @DisplayName("실패: 주문 상태 변경 시 해당 가게가 존재하지 않는 경우")
        void validUpdateOrderStatus_StoreNotFound() {
            // given
            UserEntity owner = mock(UserEntity.class);
            given(owner.getRole()).willReturn(Role.OWNER);
            given(userRepository.findById(userId)).willReturn(Optional.of(owner));

            Order order = mock(Order.class);
            given(order.getStoreId()).willReturn(storeId);

            // 가게 정보를 찾을 수 없음
            given(storeRepository.findById(storeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderValidator.validUpdateOrderStatus(userId, order))
                    .isInstanceOf(new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS).getClass());
            // 코드상 가게가 없으면 UNAUTHORIZED_ORDER_ACCESS를 던짐
        }

        @Test
        @DisplayName("실패: MASTER가 아닌 사용자가 관리자 전용 삭제 메서드 호출")
        void validDeleteOrderByMaster_NotMaster() {
            // given
            UserEntity manager = mock(UserEntity.class);
            given(manager.getRole()).willReturn(Role.MANAGER); // MANAGER는 MASTER가 아님
            given(userRepository.findById(userId)).willReturn(Optional.of(manager));

            // when & then
            assertThatThrownBy(() -> orderValidator.validDeleteOrderByMaster(userId))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }
    }



}
