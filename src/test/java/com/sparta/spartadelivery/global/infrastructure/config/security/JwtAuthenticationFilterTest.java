package com.sparta.spartadelivery.global.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JsonSecurityErrorResponder errorResponder;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Bearer 토큰이 없으면 인증을 시도하지 않고 다음 필터로 넘긴다")
    void skipWhenBearerTokenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.isCalled()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtTokenProvider, never()).getPayload(anyString());
        verifyNoInteractions(errorResponder);
    }

    @Test
    @DisplayName("유효한 토큰이면 사용자 정보를 검증하고 SecurityContext에 인증 정보를 저장한다")
    void authenticateWithValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();
        UserEntity user = createUser(1L, "customer", Role.CUSTOMER);

        when(jwtTokenProvider.getPayload("valid-token"))
                .thenReturn(new JwtTokenProvider.TokenPayload(1L, "customer", Role.CUSTOMER));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(filterChain.isCalled()).isTrue();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getAccountName()).isEqualTo("customer");
        assertThat(principal.getRole()).isEqualTo(Role.CUSTOMER);
        verifyNoInteractions(errorResponder);
    }

    @Test
    @DisplayName("토큰의 사용자를 찾을 수 없으면 인증 정보를 비우고 오류 응답을 작성한다")
    void rejectWhenUserNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer deleted-user-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();

        when(jwtTokenProvider.getPayload("deleted-user-token"))
                .thenReturn(new JwtTokenProvider.TokenPayload(1L, "customer", Role.CUSTOMER));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.isCalled()).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(errorResponder).write(
                response,
                ErrorCode.USER_NOT_FOUND.getStatus().value(),
                ErrorCode.USER_NOT_FOUND.getMessage()
        );
    }

    @Test
    @DisplayName("토큰 사용자 정보가 현재 사용자 정보와 다르면 인증 정보를 비우고 오류 응답을 작성한다")
    void rejectWhenTokenUserChanged() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();
        UserEntity user = createUser(1L, "changed", Role.CUSTOMER);

        when(jwtTokenProvider.getPayload("stale-token"))
                .thenReturn(new JwtTokenProvider.TokenPayload(1L, "customer", Role.CUSTOMER));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.isCalled()).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(errorResponder).write(
                response,
                ErrorCode.ROLE_REVALIDATION_FAILED.getStatus().value(),
                ErrorCode.ROLE_REVALIDATION_FAILED.getMessage()
        );
    }

    @Test
    @DisplayName("토큰 권한이 현재 사용자 권한과 다르면 인증 정보를 비우고 오류 응답을 작성한다")
    void rejectWhenTokenRoleChanged() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-role-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();
        UserEntity user = createUser(1L, "customer", Role.OWNER);

        when(jwtTokenProvider.getPayload("stale-role-token"))
                .thenReturn(new JwtTokenProvider.TokenPayload(1L, "customer", Role.CUSTOMER));
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.isCalled()).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(errorResponder).write(
                response,
                ErrorCode.ROLE_REVALIDATION_FAILED.getStatus().value(),
                ErrorCode.ROLE_REVALIDATION_FAILED.getMessage()
        );
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 정보를 비우고 오류 응답을 작성한다")
    void rejectInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CountingFilterChain filterChain = new CountingFilterChain();

        when(jwtTokenProvider.getPayload("invalid-token"))
                .thenThrow(new AppException(ErrorCode.INVALID_TOKEN));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.isCalled()).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(errorResponder).write(
                response,
                ErrorCode.INVALID_TOKEN.getStatus().value(),
                ErrorCode.INVALID_TOKEN.getMessage()
        );
    }

    private UserEntity createUser(Long id, String username, Role role) {
        UserEntity user = UserEntity.builder()
                .username(username)
                .nickname("nickname")
                .email(username + "@example.com")
                .password("password")
                .role(role)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static class CountingFilterChain implements FilterChain {

        private boolean called;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            called = true;
        }

        private boolean isCalled() {
            return called;
        }
    }
}
