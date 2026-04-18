package com.sparta.spartadelivery.user.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.user.application.service.UserService;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 수정 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "본인 정보 수정 API",
            description = """
                    현재 로그인한 사용자의 정보를 수정합니다.

                    **요청 가능 권한**

                    - CUSTOMER
                    - OWNER
                    - MANAGER
                    - MASTER

                    **수정 가능 필드**

                    - username
                    - nickname
                    - email
                    - password
                    - isPublic

                    **처리 정책**

                    - 이메일을 변경하는 경우 중복 이메일 검증을 수행합니다.
                    - 비밀번호를 변경하는 경우 BCrypt로 암호화해 저장합니다.
                    """
    )
    @PatchMapping("/user")
    public ResponseEntity<ApiResponse<ResUpdateUserDto>> updateMe(
            @Valid @RequestBody ReqUpdateUserDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUpdateUserDto response = userService.updateMe(request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", response));
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
    @PatchMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<ResUpdateUserDto>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody ReqUpdateUserDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUpdateUserDto response = userService.updateUser(userId, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "SUCCESS", response));
    }
}
