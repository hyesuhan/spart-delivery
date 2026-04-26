package com.sparta.spartadelivery.order.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.spartadelivery.address.config.TestConfig;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.entity.OrderItem;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDSLTestConfig.class, TestConfig.class})
public class OrderQueryRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

    private OrderQueryRepository orderQueryRepository;

    private Long customerId = 1L;
    private UUID storeId = UUID.randomUUID();
    private UUID addressId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderQueryRepository = new OrderQueryRepository(queryFactory);
    }

    @Nested
    @DisplayName("주문 서치 쿼리 성공 테스트")
    class OrderQuery_Success {

        @Test
        @DisplayName("customer은 조건 없이 전체 조회 시 페이징 처리가 정상 작동")
        void searchOrders_Pagination_Success() {
            // given
            for (int i = 0; i < 15; i++) {
                OrderItem item = OrderItem.create(UUID.randomUUID(), i+"번째 메뉴명", i+1, i*1000);
                Order order = Order.create(customerId, storeId, addressId, List.of(item), i+"번째 요청");
                em.persist(order);
            }

            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, null, null, null
            );

            // default = start pageNum = 0 & pageSize = 10
            PageRequest pageable = PageRequest.of(0, 10);

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("CUSOMTER 권한으로 조회 시 본인의 주문만 노출이 되어야 합니다.")
        void searchOrders_ByCustomer_Success() {
            // given
            saveOrderForCustomer(customerId, "치킨");
            saveOrderForCustomer(999L, "다른 사람이 시킨 치킨");

            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, null, null, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).firstItemName()).isEqualTo("치킨");
        }

        @Test
        @DisplayName("상태 필터링이 적용되어야 한다.")
        void searchOrders_StatusFilter_Success() {
            // given 1. - pending
            OrderItem item = OrderItem.create(UUID.randomUUID(), "치킨", 1, 15000);
            Order order = Order.create(customerId, storeId, addressId, List.of(item), "맛있게 해주세요.");
            em.persist(order);

            // given 2. - ACCEPTED
            OrderItem item2 = OrderItem.create(UUID.randomUUID(), "치킨", 1, 15000);
            Order order2 = Order.create(customerId, storeId, addressId, List.of(item), "맛있게 해주세요.");
            order2.updateOrderStatus();
            em.persist(order2);



            OrderSearchRequest.SearchCondition con = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, null, OrderStatus.PENDING, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(con,PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);;
            assertThat(result.getContent()).hasSize(1);

        }

        @Test
        @DisplayName("특정 store의 필터링이 걸린다.")
        void searchOrders_StoreFilter_Success() {
            OrderItem item = OrderItem.create(UUID.randomUUID(), "치킨", 1, 15000);
            Order order = Order.create(customerId, storeId, addressId, List.of(item), "맛있게 해주세요.");
            em.persist(order);

            // given 2. - ACCEPTED
            OrderItem item2 = OrderItem.create(UUID.randomUUID(), "치킨", 2, 15000);
            Order order2 = Order.create(customerId, storeId, addressId, List.of(item2), "맛있게 해주세요.");
            order2.updateOrderStatus();
            em.persist(order2);

            // given 3 - storeId 가 다름
            OrderItem item3 = OrderItem.create(UUID.randomUUID(), "불닭", 2, 15000);
            Order order3 = Order.create(customerId, UUID.randomUUID(), addressId, List.of(item3), "맛있게 해주세요.");
            order2.updateOrderStatus();
            em.persist(order3);



            OrderSearchRequest.SearchCondition con = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, storeId, null, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(con,PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PREPARING);
            assertThat(result.getContent().get(1).status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("두 필터링(스토어, 상태) 필터가 걸려야 한다.")
        void searchOrders_StatusAndStore_Success() {
            // given 1 - PENDING & storeId
            OrderItem item = OrderItem.create(UUID.randomUUID(), "치킨", 1, 15000);
            Order order = Order.create(customerId, storeId, addressId, List.of(item), "맛있게 해주세요.");
            em.persist(order);

            // given 2. - PREPARING & storeId
            OrderItem item2 = OrderItem.create(UUID.randomUUID(), "치킨", 2, 15000);
            Order order2 = Order.create(customerId, storeId, addressId, List.of(item2), "맛있게 해주세요.");
            order2.updateOrderStatus();
            em.persist(order2);

            // given 3 - Pending & randStoreId
            OrderItem item3 = OrderItem.create(UUID.randomUUID(), "불닭", 2, 15000);
            Order order3 = Order.create(customerId, UUID.randomUUID(), addressId, List.of(item3), "맛있게 해주세요.");
            order2.updateOrderStatus();
            em.persist(order3);



            OrderSearchRequest.SearchCondition con = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, storeId, OrderStatus.PENDING, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(con,PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getContent()).hasSize(1);

        }
    }

    /** 음식점 사장님 전용 필터 성공 테스트 **/
    @Nested
    @DisplayName("사장님과 마스터 권한 쿼리 테스트")
    class AdminAndOwnerQuery_Success {

        @Test
        @DisplayName("OWNER 권한: 자신의 상점(ownedStoreId) 주문만 조회되어야 합니다.")
        void searchOrders_ByOwner_Success() {
            // given
            UUID myStoreId = UUID.randomUUID();
            UUID otherStoreId = UUID.randomUUID();

            // 내 상점 주문 2개
            saveOrderWithStore(customerId, myStoreId, "내 가게 메뉴 1");
            saveOrderWithStore(customerId, myStoreId, "내 가게 메뉴 2");
            // 다른 상점 주문 1개
            saveOrderWithStore(customerId, otherStoreId, "옆 가게 메뉴");

            // 검색 조건: OWNER 권한 + 내 상점 ID 세팅
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.OWNER, 999L, myStoreId, null, null // OWNER는 userId 필터가 작동하지 않음
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(res -> res.storeId().equals(myStoreId));
        }

        @Test
        @DisplayName("MASTER 권한: 특정 상점을 지정하지 않으면 모든 상점의 주문이 조회됩니다.")
        void searchOrders_ByMaster_AllStores() {
            // given
            UUID storeA = UUID.randomUUID();
            UUID storeB = UUID.randomUUID();
            saveOrderWithStore(1L, storeA, "A상점 주문");
            saveOrderWithStore(2L, storeB, "B상점 주문");

            // 검색 조건: MASTER 권한 + 상점 지정 안함
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.MASTER, null, null, null, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("MANAGER 권한: 특정 상점 ID로 검색 시 해당 상점의 주문만 필터링됩니다.")
        void searchOrders_ByManager_WithStoreFilter() {
            // given
            UUID targetStoreId = UUID.randomUUID();
            saveOrderWithStore(1L, targetStoreId, "타겟 메뉴");
            saveOrderWithStore(1L, UUID.randomUUID(), "무시될 메뉴");

            // 검색 조건: MANAGER 권한 + 특정 상점 검색(searchStoreId)
            // searchOrders 로직 상 ownedStoreId가 null이면 searchStoreId를 사용함
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.MANAGER, null, null, null, targetStoreId
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeId()).isEqualTo(targetStoreId);
        }

        @Test
        @DisplayName("MASTER 권한: 상점 ID와 상태를 조합하여 검색할 수 있습니다.")
        void searchOrders_ByMaster_ComplexFilter() {
            // given
            UUID targetStoreId = UUID.randomUUID();
            // 1. 타겟 상점 & PENDING
            saveOrderWithStore(1L, targetStoreId, "치킨");
            // 2. 타겟 상점 & ACCEPTED
            Order acceptedOrder = saveOrderWithStore(1L, targetStoreId, "피자");
            acceptedOrder.updateOrderStatus();

            em.persist(acceptedOrder);

            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.MASTER, null, null, OrderStatus.ACCEPTED, targetStoreId
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.ACCEPTED);
            assertThat(result.getContent().get(0).storeId()).isEqualTo(targetStoreId);
        }
    }

    @Nested
    @DisplayName("주문 서치 쿼리 실패 및 에지 케이스 테스트")
    class OrderQuery_Failure {

        @Test
        @DisplayName("조회 결과가 없는 경우: 빈 페이지 객체를 반환해야 한다.")
        void searchOrders_NoResult_ReturnsEmptyPage() {
            // given: 데이터가 아무것도 없는 상태
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, customerId, null, null, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("권한 위반 케이스: CUSTOMER가 다른 사용자의 ID로 조회하면 본인 데이터가 아니므로 검색되지 않아야 한다.")
        void searchOrders_WrongCustomerId_ReturnsEmpty() {
            // given: 내 주문 하나 저장
            saveOrderWithStore(customerId, storeId, "내 메뉴");

            // 검색 조건: CUSTOMER 권한이지만, 다른 사람의 ID(999L)로 검색 시도
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.CUSTOMER, 999L, null, null, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("상태 불일치: 존재하는 주문의 상태와 다른 상태로 검색하면 검색되지 않아야 한다.")
        void searchOrders_StatusMismatch_ReturnsEmpty() {
            // given: PENDING 상태의 주문만 존재
            saveOrderWithStore(customerId, storeId, "치킨");

            // 검색 조건: delivered 상태로 검색
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.OWNER, null, storeId, OrderStatus.DELIVERED, null
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("페이징 범위를 벗어난 요청: 데이터 총량보다 큰 페이지 번호 요청 시 빈 컨텐츠를 반환한다.")
        void searchOrders_PageOutOfBounds_ReturnsEmptyContent() {
            // given: 주문 5개 존재
            for (int i = 0; i < 5; i++) {
                saveOrderWithStore(customerId, storeId, "메뉴" + i);
            }

            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.MASTER, null, null, null, null
            );

            // when: 10번째 페이지 요청 (데이터는 1페이지 분량밖에 없음)
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(10, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(5); // 전체 개수는 정확히 유지
        }

        @Test
        @DisplayName("상점 주인 권한: 다른 상점의 ID로 조회를 시도해도 내 상점(ownedStoreId) 필터가 우선 적용되어야 한다.")
        void searchOrders_OwnerSecurityCheck() {
            // given
            UUID myStoreId = UUID.randomUUID();
            UUID otherStoreId = UUID.randomUUID();

            saveOrderWithStore(customerId, myStoreId, "내 가게 주문");
            saveOrderWithStore(customerId, otherStoreId, "남의 가게 주문");

            // 검색 조건: OWNER 권한, 내 상점은 myStoreId인데 검색은 otherStoreId를 시도
            // 리포지토리 로직 상 ownedStoreId != null 이면 해당 ID만 필터링함
            OrderSearchRequest.SearchCondition cond = new OrderSearchRequest.SearchCondition(
                    Role.OWNER, null, otherStoreId, null, myStoreId
            );

            // when
            Page<OrderSearchResponse> result = orderQueryRepository.searchOrders(cond, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).storeId()).isEqualTo(myStoreId);
            assertThat(result.getContent().get(0).storeId()).isNotEqualTo(otherStoreId);
        }
    }


    private Order saveOrderWithStore(Long cId, UUID sId, String menuName) {
        OrderItem item = OrderItem.create(UUID.randomUUID(), menuName, 1, 10000);
        Order order = Order.create(cId, sId, addressId, List.of(item), "요청");
        em.persist(order);
        return order;
    }

    private void saveOrderForCustomer(Long userId, String menuName) {
        OrderItem item = OrderItem.create(UUID.randomUUID(), menuName, 1, 15000);
        Order order = Order.create(userId, storeId, addressId, List.of(item), "맛있게 해주세요.");
        em.persist(order);
    }
}
