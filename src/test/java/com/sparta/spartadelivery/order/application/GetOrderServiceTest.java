package com.sparta.spartadelivery.order.application;


import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.domain.OrderSearchValidator;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderQueryRepository;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderDetailInfo;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetOrderServiceTest {

    @InjectMocks
    private GetOrderService getOrderService;

    @Mock
    private OrderSearchValidator orderSearchValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private OrderQueryRepository orderQueryRepository;

    private final Long userId = 1L;
    private final UUID orderId = UUID.randomUUID();
    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("주문 상세 조회 (getOrderById)")
    class GetOrderById {

        @Test
        @DisplayName("성공 - 유효한 권한으로 조회 시 상세 정보를 반환한다.")
        void getOrderById_Success() {
            // given
            Order order = mock(Order.class);
            Address address = mock(Address.class);
            OrderValidateResult validateResult = new OrderValidateResult(order, address);

            given(orderSearchValidator.validOrderDetails(userId, orderId)).willReturn(validateResult);
            given(address.getAddress()).willReturn("서울시 강남구");
            given(address.getDetail()).willReturn("101호");

            // when
            OrderDetailInfo result = getOrderService.getOrderById(userId, orderId);

            // then
            assertThat(result).isNotNull();
            verify(orderSearchValidator, times(1)).validOrderDetails(userId, orderId);
        }

        @Test
        @DisplayName("실패 - Validator에서 예외 발생 시 서비스도 예외를 던진다.")
        void getOrderById_Fail_ValidatorException() {
            // given
            given(orderSearchValidator.validOrderDetails(userId, orderId))
                    .willThrow(new AppException(AuthErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> getOrderService.getOrderById(userId, orderId))
                    .isInstanceOf(AppException.class);
        }
    }

    @Nested
    @DisplayName("주문 목록 검색 (search)")
    class SearchOrders {

        @Test
        @DisplayName("성공 - CUSTOMER 권한으로 검색 시 고객용 조건이 생성된다.")
        void search_Customer_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            OrderSearchRequest check = mock(OrderSearchRequest.class);
            Page<OrderSearchResponse> expectedPage = new PageImpl<>(List.of());

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.CUSTOMER);

            given(orderQueryRepository.searchOrders(
                    any(OrderSearchRequest.SearchCondition.class),
                    any(Pageable.class)
            )).willReturn(expectedPage);

            // when
            Page<?> result = getOrderService.search(userId, check, pageable);

            // then
            assertThat(result).isEqualTo(expectedPage);
            verify(orderSearchValidator).validPageParameter(pageable);
            verify(orderQueryRepository).searchOrders(any(), eq(pageable));
        }

        @Test
        @DisplayName("성공 - OWNER 권한으로 검색 시 상점 정보를 조회하여 조건에 포함한다.")
        void search_Owner_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            Store store = mock(Store.class);
            UUID storeId = UUID.randomUUID();
            OrderSearchRequest check = mock(OrderSearchRequest.class);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.OWNER);
            given(storeRepository.findByOwner(user)).willReturn(Optional.ofNullable(store));
            given(store.getId()).willReturn(storeId);
            given(orderQueryRepository.searchOrders(any(), any())).willReturn(new PageImpl<>(List.of()));

            // when
            getOrderService.search(userId, check, pageable);

            // then
            verify(storeRepository).findByOwner(user);
            verify(orderQueryRepository).searchOrders(any(), eq(pageable));
        }

        @Test
        @DisplayName("성공 - MASTER/MANAGER 권한으로 검색 시 관리자용 조건이 생성된다.")
        void search_Admin_Success() {
            // given
            UserEntity user = mock(UserEntity.class);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(user.getRole()).willReturn(Role.MASTER);
            given(orderQueryRepository.searchOrders(any(), any())).willReturn(new PageImpl<>(List.of()));

            OrderSearchRequest request = new OrderSearchRequest(null, null);

            // when
            getOrderService.search(userId, request, pageable);

            // then
            verify(orderQueryRepository).searchOrders(any(), eq(pageable));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자가 검색 시도 시 예외가 발생한다.")
        void search_Fail_UserNotFound() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> getOrderService.search(userId, null, pageable))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 잘못된 페이지 파라미터 전달 시 Validator에서 차단된다.")
        void search_Fail_InvalidPage() {
            // given
            doThrow(new AppException(AuthErrorCode.USER_NOT_FOUND)) // 예시 에러
                    .when(orderSearchValidator).validPageParameter(pageable);

            // when & then
            assertThatThrownBy(() -> getOrderService.search(userId, null, pageable))
                    .isInstanceOf(AppException.class);
        }
    }


}
