package com.sparta.spartadelivery.user.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.user.application.service.UserDeleteService;
import com.sparta.spartadelivery.user.application.service.UserQueryService;
import com.sparta.spartadelivery.user.application.service.UserRoleService;
import com.sparta.spartadelivery.user.application.service.UserUpdateService;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserDetailDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserPageDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserRoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Admin", description = "관리자 사용자 관리 API")
public class UserAdminController {

    private final UserQueryService userQueryService;
    private final UserUpdateService userUpdateService;
    private final UserRoleService userRoleService;
    private final UserDeleteService userDeleteService;

    @Operation(
            summary = "사용자 목록 조회 API",
            description = """
                    MANAGER 또는 MASTER 권한으로 사용자 목록을 조회합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<ResUserPageDto>> getUsers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ) {
        ResUserPageDto response = userQueryService.getUsers(userPrincipal, keyword, role, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    // 관리자 권한으로 대상 사용자의 상세 정보를 조회한다.
    @Operation(
            summary = "다른 사용자 정보 상세 조회 API",
            description = """
                    MANAGER 또는 MASTER 권한으로 대상 사용자의 상세 정보를 조회합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER
                    """
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ResUserDetailDto>> getUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUserDetailDto response = userQueryService.getUser(userId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @Operation(
            summary = "다른 사용자 정보 수정 API",
            description = """
                    MANAGER 또는 MASTER 권한으로 대상 사용자의 정보를 수정합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **대상 사용자 제한**

                    - MANAGER는 CUSTOMER, OWNER 사용자 정보만 수정할 수 있습니다.
                    - MASTER는 모든 사용자 정보를 수정할 수 있습니다.

                    **수정 가능 필드**

                    - username
                    - nickname
                    - email
                    - isPublic

                    **수정 불가 필드**

                    - password
                    - role

                    **처리 정책**

                    - 이메일을 변경하는 경우 중복 이메일 검증을 수행합니다.
                    """
    )
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<ResUpdateUserDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody ReqUpdateUserDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUpdateUserDto response = userUpdateService.updateUser(userId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    // 관리자 권한으로 대상 사용자를 탈퇴 처리한다.
    @Operation(
            summary = "관리자용 사용자 탈퇴 API",
            description = """
                    MANAGER 또는 MASTER 권한으로 대상 사용자를 탈퇴 처리합니다.

                    **요청 가능 권한**

                    - MANAGER
                    - MASTER

                    **대상 사용자 제한**

                    - MANAGER는 CUSTOMER, OWNER 사용자만 탈퇴 처리할 수 있습니다.
                    - MASTER는 CUSTOMER, OWNER, MANAGER 사용자를 탈퇴 처리할 수 있습니다.
                    - MASTER 계정은 탈퇴 처리할 수 없습니다.
                    - 관리자용 사용자 탈퇴 API로 자기 자신을 탈퇴 처리할 수 없습니다.

                    **처리 정책**

                    - 실제 데이터를 삭제하지 않고 deletedAt을 기록하는 soft delete 방식으로 처리합니다.
                    """
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userDeleteService.deleteUser(userId, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(
            summary = "사용자 권한 수정 API",
            description = """
                    MASTER 권한으로 대상 사용자의 role을 수정합니다.

                    **요청 가능 권한**

                    - MASTER

                    **수정 가능 필드**

                    - role

                    **처리 정책**

                    - MASTER가 아닌 사용자는 권한을 수정할 수 없습니다.
                    - MASTER는 자기 자신의 권한을 변경할 수 없습니다.
                    """
    )
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<ResUpdateUserRoleDto>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody ReqUpdateUserRoleDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUpdateUserRoleDto response = userRoleService.updateUserRole(userId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
