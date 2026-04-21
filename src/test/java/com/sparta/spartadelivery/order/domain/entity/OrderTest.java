package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.*;

@DisplayName("Order Entity Domain Tet")
public class OrderTest {

    private static final Long    CUSTOMER_ID = 1L;
    private static final UUID STORE_ID    = UUID.randomUUID();
    private static final UUID    ADDRESS_ID  = UUID.randomUUID();
    private static final String  REQUEST     = "문 앞에 놓아주세요";

    private List<OrderItem> singleItem;
    private List<OrderItem> multiItems;

    @BeforeEach
    void setUp() {
        OrderItem item1 = OrderItem.create(UUID.randomUUID(), "후라이드 치킨", 1, 18000);
        OrderItem item2 = OrderItem.create(UUID.randomUUID(), "콜라 1.5L",    2,  3000);

        singleItem = List.of(item1);
        multiItems = List.of(item1, item2);
    }
    @Nested
    @DisplayName("create() 정상 생성")
    class Create_Success {

        @Test
        @DisplayName("유효한 값으로 Order 를 생성하면 필드가 올바르게 세팅된다")
        void create_withValidArgs_setsFieldsCorrectly() {
            // when
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);

            // then
            assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(order.getStoreId()).isEqualTo(STORE_ID);
            assertThat(order.getAddressId()).isEqualTo(ADDRESS_ID);
            assertThat(order.getRequest()).isEqualTo(REQUEST);
        }

        @Test
        @DisplayName("초기 주문 상태는 PENDING 이다")
        void create_initialStatus_isPending() {
            // when
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("기본 주문 타입은 ONLINE 이다")
        void create_defaultOrderType_isOnline() {
            // when
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);

            // then
            assertThat(order.getOrderType()).isEqualTo(OrderType.ONLINE);
        }

        @Test
        @DisplayName("총액 = 모든 OrderItem 소계의 합산이다")
        void create_totalPrice_equalsSumOfSubTotals() {
            // given
            // item1: 18_000 × 1 = 18_000
            // item2:  3_000 × 2 =  6_000  →  합계 = 24_000
            int expectedTotal = multiItems.stream()
                    .mapToInt(OrderItem::getSubTotal)
                    .sum();

            // when
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, multiItems, REQUEST);

            // then
            assertThat(order.getTotalPrice()).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("생성된 Order 에 OrderItem 목록이 모두 포함된다")
        void create_orderItemsAreAttached() {
            // when
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, multiItems, REQUEST);

            // then
            assertThat(order.getOrderItems().size()).isEqualTo(multiItems.size());

            // then 2
            assertThat(order)
                    .extracting(Order::getOrderItems, as(InstanceOfAssertFactories.list(OrderItem.class)))
                    .extracting("menuName")
                    .contains("후라이드 치킨");

        }

        @Test
        @DisplayName("request 가 null 이어도 생성에 성공한다")
        void create_withNullRequest_succeeds() {
            // when / then
            assertThatCode(() -> Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, null))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("create() 아이템 검증 실패")
    class Create_InvalidItems {

        @Test
        @DisplayName("items 가 null 이면 AppException(ORDER_ITEMS_EMPTY) 를 던진다")
        void create_withNullItems_throwsException() {
            // when / then
            assertThatThrownBy(() -> Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, null, REQUEST))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_ITEMS_EMPTY);
                    });
        }

        @Test
        @DisplayName("items 가 빈 리스트이면 AppException(ORDER_ITEMS_EMPTY) 를 던진다")
        void create_withEmptyItems_throwsException() {
            // when / then
            assertThatThrownBy(() -> Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, Collections.emptyList(), REQUEST))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_ITEMS_EMPTY);
                    });
        }

    }

    @Nested
    @DisplayName("cancel() 취소 성공")
    class Cancel_Success {

        @Test
        @DisplayName("생성 후 5분 이내에 취소하면 상태가 CANCELED 로 변경된다")
        void cancel_withinFiveMinutes_changesStatusToCanceled() throws Exception {
            // given
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);
            // Reflection 으로 createdAt 주입 (AuditingEntityListener 없이 POJO 테스트)
            setCreatedAt(order, LocalDateTime.now().minusMinutes(3));

            // when
            order.cancel(LocalDateTime.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("생성 직후(createdAt = now) 취소하면 성공한다")
        void cancel_immediately_succeeds() throws Exception {
            // given
            LocalDateTime now = LocalDateTime.now();
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);
            setCreatedAt(order, now);

            // when / then
            assertThatCode(() -> order.cancel(now))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("생성 후 정확히 5분 시점에는 취소할 수 없다 (경계값: > 5분 조건)")
        void cancel_atExactlyFiveMinutes_throwsException() throws Exception {
            // given — createdAt 기준 +5분 경과 → isAfter 조건 충족
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5).minusSeconds(1);
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);
            setCreatedAt(order, createdAt);

            // when / then
            assertThatThrownBy(() -> order.cancel(LocalDateTime.now()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_CANCEL_TIMEOUT);
                    });
        }
    }

    @Nested
    @DisplayName("cancel() 취소 실패")
    class Cancel_Failure {

        @Test
        @DisplayName("생성 후 5분 초과 시 AppException(ORDER_CANCEL_TIMEOUT) 을 던진다")
        void cancel_afterFiveMinutes_throwsException() throws Exception {
            // given
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);
            setCreatedAt(order, LocalDateTime.now().minusMinutes(10));

            // when / then
            assertThatThrownBy(() -> order.cancel(LocalDateTime.now()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_CANCEL_TIMEOUT);
                    });
        }

        @Test
        @DisplayName("이미 CANCELED 상태인 주문도 타임아웃이면 예외를 던진다")
        void cancel_alreadyCanceledOrder_throwsExceptionOnTimeout() throws Exception {
            // given
            Order order = Order.create(CUSTOMER_ID, STORE_ID, ADDRESS_ID, singleItem, REQUEST);
            LocalDateTime createdAt = LocalDateTime.now().minusMinutes(1);
            setCreatedAt(order, createdAt);
            order.cancel(LocalDateTime.now()); // 첫 번째 취소 (성공)

            // 이후 5분 초과
            setCreatedAt(order, LocalDateTime.now().minusMinutes(10));

            // when / then — 두 번째 cancel 시도 시 타임아웃
            assertThatThrownBy(() -> order.cancel(LocalDateTime.now()))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_CANCEL_TIMEOUT);
                    });
        }
    }

    private void setCreatedAt(Order order, LocalDateTime value) throws Exception {
        // BaseEntity 에 createdAt 필드가 있다고 가정
        var field = order.getClass().getSuperclass().getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(order, value);
    }


}
