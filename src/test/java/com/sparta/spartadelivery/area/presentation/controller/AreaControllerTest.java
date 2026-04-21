package com.sparta.spartadelivery.area.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.area.application.service.AreaService;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaUpdateRequest;
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

    private UsernamePasswordAuthenticationToken managerToken;
    private UsernamePasswordAuthenticationToken masterToken;

    @BeforeEach
    void setUp() throws Exception {
        managerToken = authenticationToken(Role.MANAGER);
        masterToken = authenticationToken(Role.MASTER);

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("운영 지역 등록 성공 시 201 CREATED를 반환한다")
    void createArea() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaDetailResponse response = areaResponse("Gwanghwamun", "Seoul", "Jongno-gu", true);
        given(areaService.createArea(any(AreaCreateRequest.class), any(UserPrincipal.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(managerToken))
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
    @DisplayName("운영 지역명이 중복되면 400을 반환한다")
    void createAreaWithDuplicateName() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        given(areaService.createArea(any(AreaCreateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(AreaErrorCode.DUPLICATE_AREA_NAME));

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 등록된 운영 지역명입니다."));
    }

    @Test
    @DisplayName("운영 지역 등록 요청값이 유효하지 않으면 400을 반환한다")
    void createAreaWithInvalidRequest() throws Exception {
        AreaCreateRequest request = new AreaCreateRequest("", "Seoul", "Jongno-gu", true);

        mockMvc.perform(post("/api/v1/areas")
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("운영 지역 수정 성공 시 200 OK를 반환한다")
    void updateArea() throws Exception {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", false);
        AreaDetailResponse response = areaResponse("Jongno", "Seoul", "Jongno-gu", false);
        given(areaService.updateArea(any(UUID.class), any(AreaUpdateRequest.class), any(UserPrincipal.class)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("Jongno"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @DisplayName("운영 지역 수정 요청값이 유효하지 않으면 400을 반환한다")
    void updateAreaWithInvalidRequest() throws Exception {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("", "Seoul", "Jongno-gu", true);

        mockMvc.perform(put("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("운영 지역 수정 권한이 없으면 403을 반환한다")
    void updateAreaAccessDenied() throws Exception {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        given(areaService.updateArea(any(UUID.class), any(AreaUpdateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED));

        mockMvc.perform(put("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("운영 지역을 수정할 권한이 없습니다."));
    }

    @Test
    @DisplayName("수정할 운영 지역이 없으면 404를 반환한다")
    void updateAreaNotFound() throws Exception {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        given(areaService.updateArea(any(UUID.class), any(AreaUpdateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(AreaErrorCode.AREA_NOT_FOUND));

        mockMvc.perform(put("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("운영 지역을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("운영 지역 삭제 성공 시 200 OK를 반환한다")
    void deleteArea() throws Exception {
        UUID areaId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(masterToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("운영 지역 삭제 권한이 없으면 403을 반환한다")
    void deleteAreaAccessDenied() throws Exception {
        UUID areaId = UUID.randomUUID();
        doThrow(new AppException(AreaErrorCode.AREA_DELETE_ACCESS_DENIED))
                .when(areaService).deleteArea(any(UUID.class), any(UserPrincipal.class));

        mockMvc.perform(delete("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(managerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("운영 지역을 삭제할 권한이 없습니다."));
    }

    @Test
    @DisplayName("삭제할 운영 지역이 없으면 404를 반환한다")
    void deleteAreaNotFound() throws Exception {
        UUID areaId = UUID.randomUUID();
        doThrow(new AppException(AreaErrorCode.AREA_NOT_FOUND))
                .when(areaService).deleteArea(any(UUID.class), any(UserPrincipal.class));

        mockMvc.perform(delete("/api/v1/areas/{areaId}", areaId)
                        .with(authentication(masterToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("운영 지역을 찾을 수 없습니다."));
    }

    private AreaDetailResponse areaResponse(String name, String city, String district, boolean active) {
        return new AreaDetailResponse(
                UUID.randomUUID(),
                name,
                city,
                district,
                active,
                LocalDateTime.now()
        );
    }

    private UsernamePasswordAuthenticationToken authenticationToken(Role role) {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .accountName("manager01")
                .email("manager01@example.com")
                .password("password")
                .nickname("manager")
                .role(role)
                .build();
        return new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
    }
}
