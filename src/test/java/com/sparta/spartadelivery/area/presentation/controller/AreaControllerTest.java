package com.sparta.spartadelivery.area.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.area.application.service.AreaService;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.response.AreaDetailResponse;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AreaController.class)
@Import(AreaControllerTest.TestSecurityConfig.class)
class AreaControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .securityContext(context -> context.requireExplicitSave(false));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AreaService areaService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    void setUp() throws Exception {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .accountName("manager01")
                .email("manager01@example.com")
                .password("password")
                .nickname("manager")
                .role(Role.MANAGER)
                .build();
        authToken = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Create area returns 201 CREATED")
    void createArea() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaDetailResponse response = new AreaDetailResponse(
                UUID.randomUUID(),
                "Gwanghwamun",
                "Seoul",
                "Jongno-gu",
                true,
                LocalDateTime.now()
        );
        given(areaService.createArea(any(AreaCreateRequest.class), any(UserPrincipal.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value("Gwanghwamun"))
                .andExpect(jsonPath("$.data.city").value("Seoul"))
                .andExpect(jsonPath("$.data.district").value("Jongno-gu"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("Create area with duplicate name returns 400")
    void createAreaWithDuplicateName() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        given(areaService.createArea(any(AreaCreateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(AreaErrorCode.DUPLICATE_AREA_NAME));

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 등록된 운영 지역명입니다."));
    }

    @Test
    @DisplayName("Create area with invalid request returns 400")
    void createAreaWithInvalidRequest() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("", "Seoul", "Jongno-gu", true);

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }
}
