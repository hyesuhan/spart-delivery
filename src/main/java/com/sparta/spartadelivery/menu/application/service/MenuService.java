package com.sparta.spartadelivery.menu.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.domain.entity.Menu;
import com.sparta.spartadelivery.menu.domain.repository.MenuRepository;
import com.sparta.spartadelivery.menu.exception.MenuErrorCode;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuDetailResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuListResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    // 메뉴 목록 조회
    public List<MenuListResponse> getMenusByRole(UUID storeId, UserPrincipal requester) {

        // 1) CUSTOMER
        if (requester == null || requester.getRole() == Role.CUSTOMER) {
            return menuRepository
                    .findAllByStoreIdAndDeletedAtIsNullAndIsHiddenFalse(storeId)
                    .stream()
                    .map(MenuListResponse::from)
                    .toList();
        }

        // 2) OWNER / MANAGER
        if (requester.getRole() == Role.OWNER || requester.getRole() == Role.MANAGER) {
            return menuRepository
                    .findAllByStoreIdAndDeletedAtIsNull(storeId)
                    .stream()
                    .map(MenuListResponse::from)
                    .toList();
        }

        // 3) MASTER
        if (requester.getRole() == Role.MASTER) {
            return menuRepository
                    .findAllByStoreId(storeId)
                    .stream()
                    .map(MenuListResponse::from)
                    .toList();
        }

        throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
    }

    // 메뉴 상세 조회
    public MenuDetailResponse getMenuByRole(UUID menuId, UserPrincipal requester) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new AppException(MenuErrorCode.MENU_NOT_FOUND));

        // 1) CUSTOMER
        if (requester == null || requester.getRole() == Role.CUSTOMER) {
            if (menu.getDeletedAt() != null || menu.isHidden()) {
                throw new AppException(MenuErrorCode.MENU_NOT_FOUND);
            }
            return MenuDetailResponse.from(menu);
        }

        // 2) OWNER / MANAGER
        if (requester.getRole() ==  Role.OWNER || requester.getRole() == Role.MANAGER) {
            if (menu.getDeletedAt() != null) {
                throw new AppException(MenuErrorCode.MENU_NOT_FOUND);
            }
            return MenuDetailResponse.from(menu);
        }

        // 3) MASTER → 전체 조회 가능
        if (requester.getRole() == Role.MASTER) {
            return MenuDetailResponse.from(menu);
        }

        throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
    }

    // 내부 검증 메소드
    private Menu getActiveMenu(UUID menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new AppException(MenuErrorCode.MENU_NOT_FOUND));
    }

    private void validateOwnerOrManager(UserPrincipal requester) {
        if (requester.getRole() == Role.OWNER || requester.getRole() == Role.MANAGER) return;
        throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
    }

    private void validateMaster(UserPrincipal requester) {
        if (requester.getRole() == Role.MASTER) return;
        throw new AppException(MenuErrorCode.MENU_ACCESS_DENIED);
    }

}
