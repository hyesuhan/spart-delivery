package com.sparta.spartadelivery.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.order.application.OrderOwnerService;
import com.sparta.spartadelivery.order.application.OrderService;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.controller.OrderController;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderCreateRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import com.sparta.spartadelivery.order.presentation.dto.request.UpdateOrderRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderResponse;
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

import java.util.Collections;
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

@WebMvcTest(OrderController.class)
@Import(OrderControllerTest.TestSecurityConfig.class)
@DisplayName("OrderController 테스트")
public class OrderControllerTest {

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
    private OrderService orderService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private OrderOwnerService orderOwnerService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UserPrincipal customerPrincipal;
    private UsernamePasswordAuthenticationToken customerAuthToken;
    private UUID orderId;

    @BeforeEach
    void setUp() throws Exception {
        orderId = UUID.randomUUID();

        customerPrincipal = UserPrincipal.builder()
                .id(1L)
                .accountName("customer1")
                .email("test@sparta.com")
                .nickname("고객")
                .password("pa123")
                .role(Role.CUSTOMER)
                .build();

        customerAuthToken = new UsernamePasswordAuthenticationToken(
                customerPrincipal, null, customerPrincipal.getAuthorities()
        );

        // JWT 필터 통과 설정
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // --- 성공 케이스 ---

    @Test
    @DisplayName("주문 생성 성공 - 201 CREATED")
    void createOrder_Success() throws Exception {

        UUID randomAddreddId = UUID.randomUUID();

        UUID menuRandId = UUID.randomUUID();

        // given
        OrderItemRequest item = new OrderItemRequest(menuRandId, "치킨", 1, 20000);
        OrderCreateRequest request = new OrderCreateRequest(UUID.randomUUID(), randomAddreddId, OrderType.ONLINE, "빨리요", List.of(item));



        OrderResponse response = new OrderResponse(orderId, randomAddreddId, OrderType.ONLINE, "테스트", Collections.singletonList(new OrderResponse.OrderItemResponse(menuRandId, 1)));

        given(orderService.createOrder(anyLong(), any(OrderCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(customerAuthToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));
    }

    @Test
    @DisplayName("주문 요청사항 수정 성공 - 200 OK")
    void updateOrderRequest_Success() throws Exception {
        // given
        UpdateOrderRequest request = new UpdateOrderRequest("벨 누르지 마세요");

        // when & then
        mockMvc.perform(put("/api/v1/orders/{orderId}", orderId)
                        .with(authentication(customerAuthToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("주문 취소 성공 - 200 OK")
    void cancelOrder_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId)
                        .with(authentication(customerAuthToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("SUCCESS"));
    }

    @Test
    @DisplayName("주문 삭제 성공 - 204 NO_CONTENT")
    void deleteOrder_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/{orderId}", orderId)
                        .with(authentication(customerAuthToken)))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("DELETED"));
    }

    // --- 실패 케이스 ---

    @Test
    @DisplayName("주문 수정 실패 - 주문자가 아닌 경우 (403 FORBIDDEN)")
    void updateOrderRequest_Fail_Forbidden() throws Exception {
        // given
        UpdateOrderRequest request = new UpdateOrderRequest("수정요청");
        doThrow(new AppException(OrderErrorCode.UNAUTHORIZED_ORDER_ACCESS))
                .when(orderService).updateOrderRequest(anyLong(), any(UUID.class), anyString());

        // when & then
        mockMvc.perform(put("/api/v1/orders/{orderId}", orderId)
                        .with(authentication(customerAuthToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주문 취소 실패 - 존재하지 않는 주문 (404 NOT_FOUND)")
    void cancelOrder_Fail_NotFound() throws Exception {
        // given
        doThrow(new AppException(OrderErrorCode.ORDER_NOT_FOUND))
                .when(orderService).cancelOrder(anyLong(), any(UUID.class));

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId)
                        .with(authentication(customerAuthToken)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("주문 취소 실패 - 5분 경과 시간 초과 (400 BAD_REQUEST)")
    void cancelOrder_Fail_Timeout() throws Exception {
        // given
        doThrow(new AppException(OrderErrorCode.ORDER_CANCEL_TIMEOUT))
                .when(orderService).cancelOrder(anyLong(), any(UUID.class));

        // when & then
        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId)
                        .with(authentication(customerAuthToken)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
