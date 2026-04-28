package com.sparta.spartadelivery.menu.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.menu.application.service.MenuService;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuDetailResponse;
import com.sparta.spartadelivery.menu.presentation.dto.response.MenuListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Menu", description = "메뉴 조회 API")
public class MenuController {

    private final MenuService menuService;

    @Operation(
            summary = "메뉴 목록 조회 API",
            description = """
                    특정 매장의 메뉴 목록을 조회합니다.

                    **권한별 조회 정책**

                    - CUSTOMER: 숨겨지지 않은 메뉴만 조회 가능
                    - OWNER/MANAGER: 숨겨진 메뉴를 포함하여 조회 가능
                    - MASTER: 숨겨진, 삭제된 메뉴를 포함하여 조회 가능
                    """
    )
    @GetMapping("/stores/{storeId}/menus")
    public List<MenuListResponse> getMenus(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return menuService.getMenusByRole(storeId, userPrincipal);
    }

    @Operation(
            summary = "메뉴 상세 조회 API",
            description = """
                    메뉴의 상세 정보를 조회합니다.

                    **권한별 조회 정책**

                    - CUSTOMER: 숨겨지지 않은 메뉴만 조회 가능
                    - OWNER/MANAGER: 숨겨진 메뉴를 포함하여 조회 가능
                    - MASTER: 숨겨진, 삭제된 메뉴를 포함하여 조회 가능
                    """
    )
    @GetMapping("/menus/{menuId}")
    public MenuDetailResponse getMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return menuService.getMenuByRole(menuId, userPrincipal);
    }

}
