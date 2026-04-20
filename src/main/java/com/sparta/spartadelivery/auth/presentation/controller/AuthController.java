package com.sparta.spartadelivery.auth.presentation.controller;

import com.sparta.spartadelivery.auth.application.service.AuthService;
import com.sparta.spartadelivery.auth.presentation.dto.request.ReqLoginDto;
import com.sparta.spartadelivery.auth.presentation.dto.request.ReqSignupDto;
import com.sparta.spartadelivery.auth.presentation.dto.response.ResLoginDto;
import com.sparta.spartadelivery.auth.presentation.dto.response.ResSignupDto;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입, 로그인, 현재 사용자 조회 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "새 사용자를 생성하고 비밀번호를 암호화해 저장합니다. 이메일은 중복될 수 없습니다.",
            // 전역 JWT 인증 요구사항을 공개 API(회원 가입 및 로그인 API)에서는 적용하지 않는다.
            // security = {} : 전역 security 설정을 이 API에서 비우겠다는 명시적 override
            security = {}
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<ResSignupDto>> signup(@Valid @RequestBody ReqSignupDto request) {
        ResSignupDto response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", response));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 인증한 뒤 JWT access token을 발급합니다.",
            security = {}
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<ResLoginDto>> login(@Valid @RequestBody ReqLoginDto request) {
        ResLoginDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }

    // TODO : 컨트롤러에서 현재 사용자 꺼내는 예시, 팀원들 확인이 끝나면 추후 삭제 예정
    @Operation(
            summary = "현재 사용자 조회",
            description = "Authorization 헤더의 Bearer 토큰을 검증하고 현재 로그인한 사용자 정보를 반환합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = Map.of(
                "id", userPrincipal.getId(),
                "username", userPrincipal.getAccountName(),
                "nickname", userPrincipal.getNickname(),
                "email", userPrincipal.getEmail(),
                "role", userPrincipal.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), response));
    }
}
