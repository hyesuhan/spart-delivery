package com.sparta.spartadelivery.order.domain;

import com.sparta.spartadelivery.address.domain.repository.AddressRepository;
import com.sparta.spartadelivery.address.exception.AddressErrorCode;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public void validCreateOrder(Long userId, OrderCreateRequest request) {
        validateStore(request.storeId());
        validateAddress(request.addressId(), userId);
        validateMenuPrice(request.orderItems());
    }

    public void validUpdateRequest(Long requestUserId, Order order) {

        // PENDING 일 때만 가능하다.
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(OrderErrorCode.ORDER_CALCEL_ONLY_PENDING);
        }

        // 만약 주문자와 요청자의 id가 다르면 불가능
        if (!Objects.equals(order.getCustomerId(), requestUserId)) {
            throw new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

    }

    public void validUpdateOrderStatus(Long requestUserId, Order order) {

        UserEntity user = userRepository.findById(requestUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER) {
            return;
        }

        if (user.getRole() == Role.OWNER) {
            Store store = storeRepository.findById(order.getStoreId())
                    .orElseThrow(() -> new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS));

            if (Objects.equals(store.getOwner().getId(), requestUserId)) {
                return;
            }
        }

        throw new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);

    }

    public void validCancelOrder(Long requestUserId, Order order) {
        // Master 혹은 본인만 가능하다.

        UserEntity user = userRepository.findById(requestUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        // if master can cancel
        if (user.getRole() == Role.MASTER) {
            return;
        }

        if (user.getRole() == Role.CUSTOMER) {
            if (!user.getId().equals(requestUserId)) {
                throw new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
            }
            return;
        }

        throw new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);

    }

    public String validDeleteOrderByMaster(Long requestUserId) {
        UserEntity user = userRepository.findById(requestUserId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.MASTER) {
            throw new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        return user.getUsername();
    }

    private void validateStore(UUID storeId) {
        // 추후 가게 상태에 따른 검증이 필요 합니다.
        if(!storeRepository.existsById(storeId)) {
            throw new AppException(OrderErrorCode.STORE_NOT_AVAILABLE);
        }
    }

    private void validateAddress(UUID addressId, Long customerId) {
        if (!addressRepository.existsByIdAndCustomerId(addressId, customerId)) {
            throw new AppException(AddressErrorCode.ADDRESS_NOT_FOUND);
        }
    }

    private void validateMenuPrice(List<OrderItemRequest> items) {
        // 이가 db 의 가격과 이름이 동일한지 검증

        List<UUID> menuIds = items.stream()
                .map(OrderItemRequest::menuId)
                .toList();

        List<Menu> menus = menuRepository.findAllById(menuIds);

        if (menus.size() != menuIds.size()) {
            // 존재하지 않는 메뉴가 존재한다면 error
            throw new AppException(OrderErrorCode.MENU_NOT_FOUND);
        }

        // Key: MenuId, Val: Menu Object
        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));

        for (OrderItemRequest item: items) {
            Menu menu = menuMap.get(item.menuId());

            // 1. 가격 확인
            if (!Objects.equals(menu.getPrice().getPrice(), item.unitPrice())) {
                throw new AppException(OrderErrorCode.MENU_PRICE_MISMATCH);
            }

            // 2. 이름 확인
            if(!menu.getName().equals(item.menuName())) {
                throw new AppException(OrderErrorCode.MENU_NAME_CHANGED);
            }
        }

    }

}
