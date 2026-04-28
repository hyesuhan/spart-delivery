package com.sparta.spartadelivery.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtAuthenticationFilter;
import com.sparta.spartadelivery.global.infrastructure.config.security.JwtTokenProvider;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.order.application.GetOrderService;
import com.sparta.spartadelivery.order.application.OrderOwnerService;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.OrderType;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import com.sparta.spartadelivery.order.presentation.controller.OrderSearchController;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderDetailInfo;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(OrderSearchController.class)
@Import(OrderControllerTest.TestSecurityConfig.class)
@DisplayName("Order SEARCH Controller test")
public class OrderSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetOrderService orderService;

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
    private UUID addressId;

    @Autowired
    private GetOrderService getOrderService;

    @BeforeEach
    void setUp() throws Exception {
        orderId = UUID.randomUUID();
        addressId = UUID.randomUUID();

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


    @Nested
    @DisplayName("주문 상세 조회 (getOrderById)")
    class GetOrderDetail {

        @Test
        @DisplayName("성공: 복잡한 구조의 주문 상세 정보를 정상적으로 반환한다.")
        void getOrderById_Success() throws Exception {
            // given
            UUID addressId = UUID.randomUUID();

            // 1. 내부 아이템 리스트 생성
            OrderDetailInfo.OrderItemDetailInfo item1 = new OrderDetailInfo.OrderItemDetailInfo(UUID.randomUUID(), "황금올리브 치킨", 1);
            OrderDetailInfo.OrderItemDetailInfo item2 = new OrderDetailInfo.OrderItemDetailInfo(UUID.randomUUID(), "콜라 1.5L", 1);
            List<OrderDetailInfo.OrderItemDetailInfo> itemInfos = List.of(item1, item2);

            // 2. 새로운 OrderDetailInfo 레코드 생성 (제공해주신 생성자 순서 준수)
            OrderDetailInfo response = new OrderDetailInfo(
                    orderId,                // orderId
                    addressId,              // addressId
                    "서울시 강남구 테헤란로",   // address
                    "스파르타 빌딩 5층",       // addressDetail
                    OrderType.ONLINE,       // orderType
                    OrderStatus.PENDING,    // orderStatus
                    "문 앞에 두고 벨 눌러주세요", // request
                    25000,                  // totalPrice
                    itemInfos               // infos (List)
            );

            given(getOrderService.getOrderById(anyLong(), any(UUID.class))).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                            .with(authentication(customerAuthToken)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("SUCCESS"))
                    // 데이터 매핑 검증
                    .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                    .andExpect(jsonPath("$.data.address").value("서울시 강남구 테헤란로"))
                    .andExpect(jsonPath("$.data.totalPrice").value(25000))
                    .andExpect(jsonPath("$.data.orderType").value("ONLINE"))
                    // 리스트(infos) 검증
                    .andExpect(jsonPath("$.data.infos").isArray())
                    .andExpect(jsonPath("$.data.infos[0].menuName").value("황금올리브 치킨"))
                    .andExpect(jsonPath("$.data.infos[1].menuName").value("콜라 1.5L"))
                    .andExpect(jsonPath("$.data.infos.length()").value(2));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문 ID로 상세 조회 시 404를 반환한다.")
        void getOrderById_NotFound() throws Exception {
            // given
            given(getOrderService.getOrderById(anyLong(), any(UUID.class)))
                    .willThrow(new AppException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                            .with(authentication(customerAuthToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("주문 목록 검색 (getOrders)")
    class SearchOrders {

        @Test
        @DisplayName("성공: Query Parameter를 통해 조건에 맞는 주문 목록을 페이징하여 반환한다.")
        void getOrders_Success() throws Exception {
            // given
            // 서비스 응답 데이터 준비
            OrderSearchResponse item = new OrderSearchResponse(
                    UUID.randomUUID(), 1L, UUID.randomUUID(), "테스트상점",
                    OrderStatus.PENDING, "치킨", "빨리요", LocalDateTime.now()
            );
            Page<OrderSearchResponse> pageResponse = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

            // 서비스 메서드 모킹 (컨트롤러 내부에서 생성되는 객체이므로 any() 사용)
            given(getOrderService.search(anyLong(), any(OrderSearchRequest.class), any(Pageable.class)))
                    .willReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/v1/orders")
                            .with(authentication(customerAuthToken)) // 인증 설정
                            // GET 요청이므로 .content()는 삭제하고 .param()으로 검색 조건 전달
                            .param("storeId", UUID.randomUUID().toString())
                            .param("orderStatus", "PENDING")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "createdAt,desc")) // PageableDefault와 일치
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200)) // ApiResponse 구조에 맞춤
                    .andExpect(jsonPath("$.message").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[0].storeName").value("테스트상점"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

    }
}
