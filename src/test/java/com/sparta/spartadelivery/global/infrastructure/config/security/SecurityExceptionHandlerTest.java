package com.sparta.spartadelivery.global.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.GlobalErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class SecurityExceptionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSecurityErrorResponder errorResponder = new JsonSecurityErrorResponder(objectMapper);
    private final RestAuthenticationEntryPoint authenticationEntryPoint =
            new RestAuthenticationEntryPoint(errorResponder);
    private final RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler(errorResponder);

    @Test
    @DisplayName("인증 실패 시 INVALID_TOKEN JSON 응답을 작성한다")
    void authenticationFailure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(request, response, new BadCredentialsException("invalid"));
        var responseBody = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(AuthErrorCode.INVALID_TOKEN.getStatus().value());
        assertThat(responseBody.get("status").asInt()).isEqualTo(AuthErrorCode.INVALID_TOKEN.getStatus().value());
        assertThat(responseBody.get("message").asText()).isEqualTo(AuthErrorCode.INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("인가 실패 시 ACCESS_DENIED JSON 응답을 작성한다")
    void accessDenied() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("denied"));
        var responseBody = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(GlobalErrorCode.ACCESS_DENIED.getStatus().value());
        assertThat(responseBody.get("status").asInt()).isEqualTo(GlobalErrorCode.ACCESS_DENIED.getStatus().value());
        assertThat(responseBody.get("message").asText()).isEqualTo(GlobalErrorCode.ACCESS_DENIED.getMessage());
    }
}
