package com.sparta.spartadelivery.address.presentation.controller;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.address.application.AddressService;
import com.sparta.spartadelivery.address.exception.AddressErrorCode;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import com.sparta.spartadelivery.address.presentation.dto.request.AddressUpdateRequest;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressDetailInfo;
import com.sparta.spartadelivery.address.presentation.dto.response.AddressInfo;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@Import(AddressControllerTest.TestSecurityConfig.class)
@DisplayName("AddressController 테스트")
public class AddressControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .securityContext(context -> context.requireExplicitSave(false));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal userPrincipal;
    private UUID addressId;
    private LocalDateTime now;
    private UsernamePasswordAuthenticationToken authToken; // ← 추가

    @BeforeEach
    void setUp() throws Exception{
        addressId = UUID.randomUUID();
        now = LocalDateTime.now();

        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .accountName("test1")
                .nickname("유저1")
                .email("test@sparta.com")
                .password("pa123")
                .role(Role.CUSTOMER)
                .build();

        // authentication() 방식으로 principal 직접 주입
        authToken = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        // test 시 필터가 채가고 있어 테스트가 불가능해 추가했습니다.
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(
                    invocation.getArgument(0),
                    invocation.getArgument(1)
            );
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("배송지 생성 성공 - 201 CREATED")
    void createdAddress_Success() throws Exception {
        AddressCreateRequest request =
                new AddressCreateRequest("집", "서울", "101호", "12345", true);
        AddressDetailInfo response =
                new AddressDetailInfo(addressId, "집", "서울", "101호", "12345", true, now);

        given(addressService.createAddress(any(AddressCreateRequest.class), anyLong()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/addresses")
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.alias").value("집"));
    }

    @Test
    @DisplayName("배송지 상세 조회 성공 - 200 OK")
    void getAddressDetail_Success() throws Exception {
        AddressDetailInfo response =
                new AddressDetailInfo(addressId, "집", "서울", "101", "123", true, now);
        given(addressService.getAddress(any(UUID.class), anyLong()))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alias").value("집"));
    }

    @Test
    @DisplayName("배송지 전체 조회 성공 - 200 OK")
     void getAddressList_Success() throws Exception {
        // given
        AddressDetailInfo response1 =
                new AddressDetailInfo(UUID.randomUUID(), "집", "서울", "101", "123", true, now);
        AddressDetailInfo response2 =
                new AddressDetailInfo(UUID.randomUUID(), "회사", "경기", "202", "456", false, now);

        AddressInfo res1 = new AddressInfo(response1.id(), response1.alias(), response1.address());
        AddressInfo res2 = new AddressInfo(response2.id(), response2.alias(), response2.address());

        given(addressService.getAddresses(anyLong()))
                .willReturn(List.of(res1, res2));

        // when & then
        mockMvc.perform(get("/api/v1/addresses")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].alias").value("집"))
                .andExpect(jsonPath("$.data[1].alias").value("회사"));
    }

    @Test
    @DisplayName("배송지 수정 성공 - 200 OK")
    void updateAddress_Success() throws Exception {
        AddressUpdateRequest request =
                new AddressUpdateRequest("회사", "경기", "2층", "55555", false);
        AddressInfo response =
                new AddressInfo(addressId, "회사", "경기");

        given(addressService.updatedAddress(any(UUID.class), any(AddressUpdateRequest.class), anyLong()))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alias").value("회사"))
                .andExpect(jsonPath("$.data.address").value("경기"));
    }

    @Test
    @DisplayName("배송지 삭제 성공 - 204 NO_CONTENT")
    void deleteAddress_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(authToken)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("DELETED"));
    }

    @Test
    @DisplayName("배송지 삭제 성공 - MASTER 권한 (204 NO_CONTENT)")
    void deleteAddress_Success_Master() throws Exception {
        // given
        UserPrincipal masterPrincipal = UserPrincipal.builder()
                .id(99L)                    // 주소 소유자와 다른 ID
                .accountName("master1")
                .nickname("마스터")
                .email("master@sparta.com")
                .password("pa123")
                .role(Role.MASTER)          // MASTER 권한
                .build();

        UsernamePasswordAuthenticationToken masterToken = new UsernamePasswordAuthenticationToken(
                masterPrincipal,
                null,
                masterPrincipal.getAuthorities()
        );

        // MASTER는 서비스에서 예외 없이 정상 처리 (void 메서드 → stub 불필요)

        // when & then
        mockMvc.perform(delete("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(masterToken)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("DELETED"));
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 다른 사용자의 CUSTOMER 권한 (403 FORBIDDEN)")
    void deleteAddress_Fail_OtherCustomer() throws Exception {
        // given
        UserPrincipal otherCustomer = UserPrincipal.builder()
                .id(999L)                   // 주소 소유자(1L)와 다른 ID
                .accountName("other1")
                .nickname("다른유저")
                .email("other@sparta.com")
                .password("pa123")
                .role(Role.CUSTOMER)
                .build();

        UsernamePasswordAuthenticationToken otherToken = new UsernamePasswordAuthenticationToken(
                otherCustomer,
                null,
                otherCustomer.getAuthorities()
        );

        // 다른 CUSTOMER가 접근하면 서비스에서 ACCESS_DENIED 예외 발생
        doThrow(new AppException(AddressErrorCode.ADDRESS_ACCESS_DENIED, "해당 주소에 대한 접근 권한이 없습니다."))
                .when(addressService).deleteAddress(any(UUID.class), eq(999L));

        // when & then
        mockMvc.perform(delete("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(otherToken)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 다른 사용자의 MANAGER 권한 (403 FORBIDDEN)")
    void deleteAddress_Fail_OtherManager() throws Exception {
        // given
        UserPrincipal managerPrincipal = UserPrincipal.builder()
                .id(888L)                   // 주소 소유자(1L)와 다른 ID
                .accountName("manager1")
                .nickname("매니저")
                .email("manager@sparta.com")
                .password("pa123")
                .role(Role.MANAGER)
                .build();

        UsernamePasswordAuthenticationToken managerToken = new UsernamePasswordAuthenticationToken(
                managerPrincipal,
                null,
                managerPrincipal.getAuthorities()
        );

        // MANAGER가 다른 사용자 주소 접근 시 서비스에서 ACCESS_DENIED 예외 발생
        doThrow(new AppException(AddressErrorCode.ADDRESS_ACCESS_DENIED, "해당 주소에 대한 접근 권한이 없습니다."))
                .when(addressService).deleteAddress(any(UUID.class), eq(888L));

        // when & then
        mockMvc.perform(delete("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(managerToken)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("배송지 조회 실패 - 404")
    void getAddressDetail_Fail_NotFound() throws Exception {
        given(addressService.getAddress(any(UUID.class), anyLong()))
                .willThrow(new AppException(AddressErrorCode.ADDRESS_NOT_FOUND, "주소를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(authToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배송지 수정 실패 - 403")
    void updateAddress_Fail_AccessDenied() throws Exception {
        AddressUpdateRequest request =
                new AddressUpdateRequest("회사", "경기", "2층", "55555", false);
        given(addressService.updatedAddress(any(UUID.class), any(AddressUpdateRequest.class), anyLong()))
                .willThrow(new AppException(AddressErrorCode.ADDRESS_ACCESS_DENIED, "접근 권한이 없습니다."));

        mockMvc.perform(put("/api/v1/addresses/{addressId}", addressId)
                        .with(authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("기본 배송지 설정 성공 - 200 OK")
    void setDefaultAddress_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/addresses/{addressId}/default", addressId)
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }
}