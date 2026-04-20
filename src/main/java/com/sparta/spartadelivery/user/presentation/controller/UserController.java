package com.sparta.spartadelivery.user.presentation.controller;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import com.sparta.spartadelivery.user.application.service.UserDeleteService;
import com.sparta.spartadelivery.user.application.service.UserQueryService;
import com.sparta.spartadelivery.user.application.service.UserUpdateService;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserDetailDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 본인 정보 API")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserUpdateService userUpdateService;
    private final UserDeleteService userDeleteService;

    // 로그인한 사용자의 본인 상세 정보를 조회한다.
    @Operation(
            summary = "본인 정보 상세 조회 API",
            description = """
                    현재 로그인한 사용자의 상세 정보를 조회합니다.

                    **요청 가능 권한**

                    - CUSTOMER
                    - OWNER
                    - MANAGER
                    - MASTER
                    """
    )
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<ResUserDetailDto>> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ResUserDetailDto response = userQueryService.getMe(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    // 로그인한 사용자의 본인 계정을 탈퇴 처리한다.
    @Operation(
            summary = "본인 회원 탈퇴 API",
            description = """
                    현재 로그인한 사용자의 계정을 탈퇴 처리합니다.

                    **요청 가능 권한**

                    - CUSTOMER
                    - OWNER
                    - MANAGER
                    - MASTER

                    **처리 정책**

                    - 실제 데이터를 삭제하지 않고 deletedAt을 기록하는 soft delete 방식으로 처리합니다.
                    """
    )
    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Void>> deleteMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userDeleteService.deleteMe(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), null));
    }

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
        ResUpdateUserDto response = userUpdateService.updateMe(request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
