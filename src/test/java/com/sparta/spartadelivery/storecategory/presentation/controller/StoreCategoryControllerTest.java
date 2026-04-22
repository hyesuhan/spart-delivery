package com.sparta.spartadelivery.storecategory.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.storecategory.application.service.StoreCategoryService;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryDetailResponse;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryListResponse;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryPageResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
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

@WebMvcTest(StoreCategoryController.class)
@Import(StoreCategoryControllerTest.TestSecurityConfig.class)
class StoreCategoryControllerTest {

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
    private StoreCategoryService storeCategoryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken managerToken;

    @BeforeEach
    void setUp() throws Exception {
        managerToken = authenticationToken(Role.MANAGER);

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("가게 카테고리 등록 성공 시 201 CREATED를 반환한다")
    void createStoreCategory() throws Exception {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("한식");
        StoreCategoryDetailResponse response = storeCategoryResponse("한식");
        given(storeCategoryService.createStoreCategory(any(StoreCategoryCreateRequest.class), any(UserPrincipal.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/store-categories")
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value("한식"));
    }

    @Test
    @DisplayName("가게 카테고리명이 중복되면 400을 반환한다")
    void createStoreCategoryWithDuplicateName() throws Exception {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("한식");
        given(storeCategoryService.createStoreCategory(any(StoreCategoryCreateRequest.class), any(UserPrincipal.class)))
                .willThrow(new AppException(StoreCategoryErrorCode.DUPLICATE_STORE_CATEGORY_NAME));

        mockMvc.perform(post("/api/v1/store-categories")
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 등록된 가게 카테고리명입니다."));
    }

    @Test
    @DisplayName("가게 카테고리 등록 요청값이 유효하지 않으면 400을 반환한다")
    void createStoreCategoryWithInvalidRequest() throws Exception {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("");

        mockMvc.perform(post("/api/v1/store-categories")
                        .with(authentication(managerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("가게 카테고리 목록 조회 성공 시 200 OK를 반환한다")
    void getStoreCategories() throws Exception {
        StoreCategoryPageResponse response = new StoreCategoryPageResponse(
                List.of(
                        storeCategoryListResponse("한식"),
                        storeCategoryListResponse("치킨")
                ),
                0,
                10,
                2,
                1,
                "createdAt,DESC"
        );
        given(storeCategoryService.getStoreCategories(0, 10, null)).willReturn(response);

        mockMvc.perform(get("/api/v1/store-categories")
                        .with(authentication(managerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].name").value("한식"))
                .andExpect(jsonPath("$.data.content[1].name").value("치킨"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.sort").value("createdAt,DESC"));
    }

    @Test
    @DisplayName("가게 카테고리 목록 조회 시 잘못된 페이지 번호면 400을 반환한다")
    void getStoreCategoriesWithInvalidPageNumber() throws Exception {
        given(storeCategoryService.getStoreCategories(-1, 10, null))
                .willThrow(new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_PAGE_NUMBER));

        mockMvc.perform(get("/api/v1/store-categories")
                        .with(authentication(managerToken))
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    @DisplayName("가게 카테고리 상세 조회 성공 시 200 OK를 반환한다")
    void getStoreCategory() throws Exception {
        UUID storeCategoryId = UUID.randomUUID();
        StoreCategoryDetailResponse response = storeCategoryResponse("한식");
        given(storeCategoryService.getStoreCategory(any(UUID.class))).willReturn(response);

        mockMvc.perform(get("/api/v1/store-categories/{storeCategoryId}", storeCategoryId)
                        .with(authentication(managerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("한식"));
    }

    @Test
    @DisplayName("상세 조회 대상 가게 카테고리가 없으면 404를 반환한다")
    void getStoreCategoryNotFound() throws Exception {
        UUID storeCategoryId = UUID.randomUUID();
        given(storeCategoryService.getStoreCategory(any(UUID.class)))
                .willThrow(new AppException(StoreCategoryErrorCode.STORE_CATEGORY_NOT_FOUND));

        mockMvc.perform(get("/api/v1/store-categories/{storeCategoryId}", storeCategoryId)
                        .with(authentication(managerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("가게 카테고리를 찾을 수 없습니다."));
    }

    private StoreCategoryDetailResponse storeCategoryResponse(String name) {
        return new StoreCategoryDetailResponse(
                UUID.randomUUID(),
                name,
                LocalDateTime.now()
        );
    }

    private StoreCategoryListResponse storeCategoryListResponse(String name) {
        return new StoreCategoryListResponse(
                UUID.randomUUID(),
                name,
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
