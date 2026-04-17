package com.sparta.spartadelivery.global.infrastructure.config.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    // 애플리케이션의 HTTP 보안 정책 구성

    // 이 프로젝트는 세션이 아니라 JWT access token으로 사용자를 인증하므로,
    // Spring Security의 기본 로그인 화면, HTTP Basic 인증, 서버 세션을 사용하지 않고
    // 요청마다 Authorization 헤더의 Bearer 토큰을 검증하는 방식으로 동작한다.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {
        http
                // JWT는 쿠키 기반 세션 인증을 사용하지 않으므로 CSRF 보호 대상에서 제외한다.
                .csrf(AbstractHttpConfigurer::disable)

                // REST API 서버에서는 브라우저 기본 인증 팝업과 서버 렌더링 로그인 폼을 사용하지 않는다.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // CORS 세부 정책은 CorsConfigurationSource 빈이나 Spring MVC 설정이 있으면 그 설정을 따른다.
                .cors(Customizer.withDefaults())

                // 서버가 인증 상태를 세션에 저장하지 않도록 설정한다. 모든 요청은 JWT로 다시 인증된다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Security 필터 단계의 인증/인가 실패는 GlobalExceptionHandler가 처리하지 못하므로 전용 핸들러로 JSON 응답을 작성한다.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )

                // 회원가입과 로그인은 토큰 발급 전 요청이므로 열어두고, 나머지 API는 인증된 사용자만 접근하게 한다.
                .authorizeHttpRequests(authorize -> authorize
                        // API 문서는 인증 전에도 확인할 수 있어야 하므로 Swagger UI와 OpenAPI JSON 경로는 열어둔다.
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup", "/api/v1/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )

                // UsernamePasswordAuthenticationFilter보다 먼저 JWT를 검증해 SecurityContext에 인증 객체를 채운다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 로그인 처리에서 AuthenticationManager가 필요할 때 주입받아 사용할 수 있도록 Bean으로 노출
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 사용자 비밀번호를 단방향 해시로 저장하고 검증하기 위한 PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
