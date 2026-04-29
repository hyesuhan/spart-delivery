package com.sparta.spartadelivery.store.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.store.application.service.StoreService;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreCreateRequest;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreUpdateRequest;
import com.sparta.spartadelivery.store.presentation.dto.response.StoreDetailResponse;
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
import org.springframework.http.MediaType;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken ownerToken;
    private UsernamePasswordAuthenticationToken customerToken;
    private UsernamePasswordAuthenticationToken managerToken;

    @BeforeEach
    void setUp() throws Exception {
        ownerToken = authenticationToken(1L, Role.OWNER);
        customerToken = authenticationToken(2L, Role.CUSTOMER);
        managerToken = authenticationToken(3L, Role.MANAGER);

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("가게 등록 성공 시 201 CREATED를 반환한다")
    void createStore() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );
        StoreDetailResponse response = storeResponse(request.name(), request.storeCategoryId(), request.areaId(), false);

        given(storeService.createStore(any(StoreCreateRequest.class), any(UserPrincipal.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/stores")
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value("스파르타 분식"));
    }

    @Test
    @DisplayName("가게 등록 요청값이 유효하지 않으면 400을 반환한다")
    void createStoreWithInvalidRequest() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest(
                null,
                UUID.randomUUID(),
                "",
                "",
                "02-1234-5678"
        );

        mockMvc.perform(post("/api/v1/stores")
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("OWNER가 아니면 가게 등록 시 403을 반환한다")
    void createStoreByNonOwnerDenied() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        given(storeService.createStore(any(StoreCreateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(StoreErrorCode.STORE_CREATE_OWNER_ROLE_REQUIRED));

        mockMvc.perform(post("/api/v1/stores")
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("지역이 없으면 가게 등록 시 404를 반환한다")
    void createStoreWhenAreaNotFound() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        given(storeService.createStore(any(StoreCreateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(AreaErrorCode.AREA_NOT_FOUND));

        mockMvc.perform(post("/api/v1/stores")
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("가게 수정 성공 시 200 OK를 반환한다")
    void updateStore() throws Exception {
        UUID storeId = UUID.randomUUID();
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울특별시 서초구 강남대로 321",
                "02-9876-5432"
        );
        StoreDetailResponse response = storeResponse(request.name(), request.storeCategoryId(), request.areaId(), false);

        given(storeService.updateStore(any(UUID.class), any(StoreUpdateRequest.class), any(UserPrincipal.class)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/stores/{storeId}", storeId)
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("스파르타 떡볶이"));
    }

    @Test
    @DisplayName("가게 수정 요청값이 유효하지 않으면 400을 반환한다")
    void updateStoreWithInvalidRequest() throws Exception {
        UUID storeId = UUID.randomUUID();
        StoreUpdateRequest request = new StoreUpdateRequest(
                null,
                UUID.randomUUID(),
                "",
                "",
                "02-9876-5432"
        );

        mockMvc.perform(put("/api/v1/stores/{storeId}", storeId)
                        .with(authentication(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("권한이 없으면 가게 수정 시 403을 반환한다")
    void updateStoreDenied() throws Exception {
        UUID storeId = UUID.randomUUID();
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울특별시 서초구 강남대로 321",
                "02-9876-5432"
        );

        given(storeService.updateStore(any(UUID.class), any(StoreUpdateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(StoreErrorCode.STORE_UPDATE_ACCESS_DENIED));

        mockMvc.perform(put("/api/v1/stores/{storeId}", storeId)
                        .with(authentication(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 숨김 처리 성공 시 200 OK를 반환한다")
    void hideStore() throws Exception {
        UUID storeId = UUID.randomUUID();
        StoreDetailResponse response = storeResponse("스파르타 분식", UUID.randomUUID(), UUID.randomUUID(), true);

        given(storeService.hideStore(any(UUID.class), any(UserPrincipal.class)))
                .willReturn(response);

        mockMvc.perform(patch("/api/v1/stores/{storeId}/hide", storeId)
                        .with(authentication(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.hidden").value(true));
    }

    @Test
    @DisplayName("권한이 없으면 가게 숨김 처리 시 403을 반환한다")
    void hideStoreDenied() throws Exception {
        UUID storeId = UUID.randomUUID();
        given(storeService.hideStore(any(UUID.class), any(UserPrincipal.class)))
                .willThrow(new AppException(StoreErrorCode.STORE_HIDE_ACCESS_DENIED));

        mockMvc.perform(patch("/api/v1/stores/{storeId}/hide", storeId)
                        .with(authentication(customerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 상세조회 성공 시 200 OK를 반환한다")
    void getStore() throws Exception {
        UUID storeId = UUID.randomUUID();
        StoreDetailResponse response = new StoreDetailResponse(
                storeId,
                1L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678",
                BigDecimal.valueOf(4.5),
                false,
                LocalDateTime.now()
        );
        given(storeService.getStore(storeId)).willReturn(response);

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .with(authentication(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(storeId.toString()))
                .andExpect(jsonPath("$.data.name").value("스파르타 분식"));
    }

    @Test
    @DisplayName("존재하지 않는 가게면 상세조회 시 404를 반환한다")
    void getStoreWhenNotFound() throws Exception {
        UUID storeId = UUID.randomUUID();
        given(storeService.getStore(storeId))
                .willThrow(new AppException(StoreErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .with(authentication(customerToken)))
                .andExpect(status().isNotFound());
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
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @DisplayName("일반 가게 목록 조회 시 잘못된 페이지 번호면 400을 반환한다")
    void getStoresWithInvalidPageNumber() throws Exception {
        given(storeService.getStores(-1, 10, null))
                .willThrow(new AppException(StoreErrorCode.STORE_LIST_INVALID_PAGE_NUMBER));

        mockMvc.perform(get("/api/v1/stores")
                        .with(authentication(customerToken))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("관리자용 가게 목록 조회 시 hidden이 false면 숨김 제외 결과를 반환한다")
    void getAdminStoresWithoutHidden() throws Exception {
        StorePageResponse response = new StorePageResponse(
                List.of(storeListResponse("스파르타 분식", "분식", "강남")),
                0,
                10,
                1,
                1,
                "createdAt,DESC"
        );
        given(storeService.getAdminStores(any(UserPrincipal.class), any(Integer.class), any(Integer.class), any(), any(Boolean.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(authentication(managerToken))
                        .param("hidden", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].hidden").value(false));
    }

    @Test
    @DisplayName("관리자용 가게 목록 조회 시 hidden이 true면 숨김 포함 결과를 반환한다")
    void getAdminStoresWithHidden() throws Exception {
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
        given(storeService.getAdminStores(any(UserPrincipal.class), any(Integer.class), any(Integer.class), any(), any(Boolean.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(authentication(managerToken))
                        .param("hidden", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[1].hidden").value(true));
    }

    @Test
    @DisplayName("관리자 권한이 없으면 관리자용 가게 목록 조회 시 403을 반환한다")
    void getAdminStoresByCustomerDenied() throws Exception {
        given(storeService.getAdminStores(any(UserPrincipal.class), any(Integer.class), any(Integer.class), any(), any(Boolean.class)))
                .willThrow(new AppException(StoreErrorCode.STORE_ADMIN_LIST_ACCESS_DENIED));

        mockMvc.perform(get("/api/v1/stores/admin")
                        .with(authentication(customerToken))
                        .param("hidden", "true"))
                .andExpect(status().isForbidden());
    }

    private StoreDetailResponse storeResponse(String name, UUID storeCategoryId, UUID areaId, boolean hidden) {
        return new StoreDetailResponse(
                UUID.randomUUID(),
                1L,
                storeCategoryId,
                areaId,
                name,
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678",
                BigDecimal.ZERO,
                hidden,
                LocalDateTime.now()
        );
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

    private UsernamePasswordAuthenticationToken authenticationToken(Long id, Role role) {
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(id)
                .accountName(role == Role.OWNER ? "owner01" : "customer01")
                .email(role == Role.OWNER ? "owner01@example.com" : "customer01@example.com")
                .password("password")
                .nickname(role == Role.OWNER ? "점주01" : "고객01")
                .role(role)
                .build();
        return new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );
    }
}
