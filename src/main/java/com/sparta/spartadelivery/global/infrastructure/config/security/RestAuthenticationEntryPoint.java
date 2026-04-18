package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;



// 인증되지 않은 사용자가 보호된 API에 접근했을 때 실행되는 Security 예외 핸들러
// 큰이 없거나, 만료되었거나, 올바르지 않은 토큰으로 요청하면,
// 컨트롤러까지 도달하지 못하므로 여기서 공통 JSON 오류 응답을 작성한다.
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {

    private final JsonSecurityErrorResponder errorResponder;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // 인증 실패는 클라이언트가 다시 로그인하거나 유효한 토큰을 보내야 하는 상황으로 처리한다.
        errorResponder.write(
                response,
                AuthErrorCode.INVALID_TOKEN.getStatus().value(),
                AuthErrorCode.INVALID_TOKEN.getMessage()
        );
    }
}
