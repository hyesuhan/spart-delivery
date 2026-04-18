package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// 요청의 Authorization 헤더에 담긴 JWT를 검증하고 Spring Security 인증 정보를 구성하는 필터
// 토큰이 없으면 인증이 필요 없는 요청일 수 있으므로 다음 필터로 넘기고,
// 유효한 토큰이면 DB의 최신 사용자 정보와 비교한 뒤 SecurityContext에 UserPrincipal을 저장한다.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JsonSecurityErrorResponder errorResponder;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Bearer 토큰이 없는 요청은 여기서 실패시키지 않고 SecurityConfig의 인가 규칙에 맡긴다.
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            JwtTokenProvider.TokenPayload tokenPayload = jwtTokenProvider.getPayload(token);
            UserEntity user = userRepository.findByIdAndDeletedAtIsNull(tokenPayload.userId())
                    .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

            // 토큰 발급 이후 사용자의 username/role이 바뀐 경우 기존 토큰을 더 이상 신뢰하지 않는다.
            if (user.getRole() != tokenPayload.role() || !user.getUsername().equals(tokenPayload.username())) {
                throw new AppException(AuthErrorCode.ROLE_REVALIDATION_FAILED);
            }

            // 인증이 완료된 사용자 정보를 이후 컨트롤러와 @AuthenticationPrincipal에서 사용할 수 있게 저장한다.
            UserPrincipal userPrincipal = UserPrincipal.from(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AppException exception) {
            // 필터 단계의 예외는 GlobalExceptionHandler까지 도달하지 않으므로 직접 JSON 응답을 작성한다.
            SecurityContextHolder.clearContext();
            errorResponder.write(
                    response,
                    exception.getErrorCode().getStatus().value(),
                    exception.getMessage()
            );
        }
    }
}
