package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.global.presentation.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Spring Security 필터 단계에서 발생한 인증/인가 오류를 공통 JSON 응답 형식으로 내려주는 컴포넌트
@Component
public class JsonSecurityErrorResponder {

    private final ObjectMapper objectMapper;

    public JsonSecurityErrorResponder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    // Security 예외는 컨트롤러에 도달하기 전에 처리될 수 있어 GlobalExceptionHandler가 잡지 못한다.
    // 본 클래스는 그런 경우에도 클라이언트가 동일한 ApiResponse 구조로 오류를 받을 수 있게 합니다.
    public void write(HttpServletResponse response, int status, String message) throws IOException {
        // Security 핸들러에서 재사용할 수 있도록 상태 코드와 메시지만 받아 공통 오류 응답을 작성한다.
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(status, message, null));
    }
}
