package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

// 인증은 되었지만 필요한 권한이 부족할 때 실행되는 Security 예외 핸들러
// 로그인 자체는 성공했지만 관리자 API처럼 더 높은 권한이 필요한 요청을 보낸 경우,
// 컨트롤러까지 도달하지 못하므로 여기서 공통 JSON 오류 응답을 작성한다.
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    private final JsonSecurityErrorResponder errorResponder;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        // 인가 실패는 인증된 사용자라도 현재 권한으로는 접근할 수 없는 상황으로 처리한다.
        errorResponder.write(
                response,
                GlobalErrorCode.ACCESS_DENIED.getStatus().value(),
                GlobalErrorCode.ACCESS_DENIED.getMessage()
        );
    }
}
