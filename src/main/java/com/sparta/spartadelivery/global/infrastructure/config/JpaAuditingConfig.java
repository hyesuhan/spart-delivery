package com.sparta.spartadelivery.global.infrastructure.config;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    private static final String SYSTEM_AUDITOR = "SYSTEM";

    // JPA Auditing이 @CreatedBy, @LastModifiedBy 값을 채울 때 사용할 현재 작업자 정보를 제공한다.
    // JWT 인증 요청이면 SecurityContext의 UserPrincipal에서 도메인 username(accountName)을 꺼내고,
    // 회원가입처럼 인증 전 요청이거나 시스템 작업이면 "SYSTEM"을 기록한다.
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> !(authentication instanceof AnonymousAuthenticationToken))
                .map(Authentication::getPrincipal)
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .map(UserPrincipal::getAccountName)
                .or(() -> Optional.of(SYSTEM_AUDITOR));
    }
}
