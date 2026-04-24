package com.sparta.spartadelivery.store.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.store.application.service.StoreService;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.store.presentation.dto.response.StoreListResponse;
import com.sparta.spartadelivery.store.presentation.dto.response.StorePageResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@Import(StoreControllerTest.TestSecurityConfig.class)
class StoreControllerTest {

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

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken customerToken;

    @BeforeEach
    void setUp() throws Exception {
        customerToken = authenticationToken(Role.CUSTOMER);

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("일반 가게 목록 조회 성공 시 200 OK를 반환한다")
    void getStores() throws Exception {
        StorePageResponse response = new StorePageResponse(
                List.of(
                        storeListResponse("스파르타 분식", "분식", "강남"),
                        storeListResponse("스파르타 치킨", "치킨", "서초")
                ),
                0,
                10,
                2,
                1,
                "createdAt,DESC"
        );
        given(storeService.getStores(0, 10, null)).willReturn(response);

        mockMvc.perform(get("/api/v1/stores")
                        .with(authentication(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].name").value("스파르타 분식"))
                .andExpect(jsonPath("$.data.content[0].storeCategoryName").value("분식"))
                .andExpect(jsonPath("$.data.content[1].name").value("스파르타 치킨"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.sort").value("createdAt,DESC"));
    }

    @Test
    @DisplayName("일반 가게 목록 조회 시 잘못된 페이지 번호면 400을 반환한다")
    void getStoresWithInvalidPageNumber() throws Exception {
        given(storeService.getStores(-1, 10, null))
                .willThrow(new AppException(StoreErrorCode.STORE_LIST_INVALID_PAGE_NUMBER));

        mockMvc.perform(get("/api/v1/stores")
                        .with(authentication(customerToken))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    @DisplayName("일반 가게 목록 조회 시 잘못된 정렬 조건이면 400을 반환한다")
    void getStoresWithInvalidSortFormat() throws Exception {
        given(storeService.getStores(0, 10, "createdAt"))
                .willThrow(new AppException(StoreErrorCode.STORE_LIST_INVALID_SORT_FORMAT));

        mockMvc.perform(get("/api/v1/stores")
                        .with(authentication(customerToken))
                        .param("sort", "createdAt"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정렬 조건은 {property},{direction} 형식이어야 합니다."));
    }

    @Test
    @DisplayName("관리자용 가게 목록 조회 성공 시 200 OK를 반환한다")
    void getAdminStores() throws Exception {
        StorePageResponse response = new StorePageResponse(
                List.of(
                        storeListResponse("스파르타 분식", "분식", "강남"),
                        hiddenStoreListResponse("스파르타 치킨", "치킨", "서초")
                ),
                0,
                10,
                2,
                1,
                "createdAt,DESC"
        );
        UsernamePasswordAuthenticationToken managerToken = authenticationToken(Role.MANAGER);
        given(storeService.getAdminStores(any(UserPrincipal.class), any(Integer.class), any(Integer.class), any()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(authentication(managerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[1].hidden").value(true));
    }

    @Test
    @DisplayName("관리자 권한이 없으면 관리자용 가게 목록 조회 시 403을 반환한다")
    void getAdminStoresByCustomerDenied() throws Exception {
        given(storeService.getAdminStores(any(UserPrincipal.class), any(Integer.class), any(Integer.class), any()))
                .willThrow(new AppException(StoreErrorCode.STORE_ADMIN_LIST_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(authentication(customerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("관리자용 가게 목록을 조회할 권한이 없습니다."));
    }

    private StoreListResponse storeListResponse(String name, String storeCategoryName, String areaName) {
        return new StoreListResponse(
                UUID.randomUUID(),
                storeCategoryName,
                areaName,
                name,
                BigDecimal.ZERO,
                false,
                LocalDateTime.now()
        );
    }

    private StoreListResponse hiddenStoreListResponse(String name, String storeCategoryName, String areaName) {
        return new StoreListResponse(
                UUID.randomUUID(),
                storeCategoryName,
                areaName,
                name,
                BigDecimal.ZERO,
                true,
                LocalDateTime.now()
        );
    }

    private UsernamePasswordAuthenticationToken authenticationToken(Role role) {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(1L)
                .accountName("customer01")
                .email("customer01@example.com")
                .password("password")
                .nickname("고객01")
                .role(role)
                .build();
        return new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
    }
}
