package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@DisplayName("OrderItem domain entity test")
public class OrderItemsTest {

    private static final UUID   MENU_ID    = UUID.randomUUID();
    private static final String MENU_NAME  = "후라이드 치킨";
    private static final int    QUANTITY   = 2;
    private static final int    UNIT_PRICE = 18_000;

    @Nested
    @DisplayName("create() 생성 성공 테스트")
    class Create_Success {

        @Test
        @DisplayName("유효한 값으로 OrderItem 을 생성하면 필드가 올바르게 생성됭야 한다.")
        void create_withValidArgs() {
            // when
            OrderItem item = OrderItem.create(MENU_ID, MENU_NAME, QUANTITY, UNIT_PRICE);

            // then
            assertThat(item.getMenuId()).isEqualTo(MENU_ID);
            assertThat(item.getMenuName()).isEqualTo(MENU_NAME);
            assertThat(item.getQuantity()).isEqualTo(QUANTITY);
            assertThat(item.getUnitPrice()).isEqualTo(UNIT_PRICE);
        }

        @Test
        @DisplayName("단가가 0원이어도 생성이 허용되어야 합니다.")
        void create_withZeroUnitPrice_succeeds() {
            // when / then
            assertThatCode(() -> OrderItem.create(MENU_ID, MENU_NAME, 1, 0))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("수량이 1개일 때에는 생성이 성공해야 합니다.")
        void create_withMinimumQuantity_succeeds() {
            // when / then
            assertThatCode(() -> OrderItem.create(MENU_ID, MENU_NAME, 1, UNIT_PRICE))
                    .doesNotThrowAnyException();
        }

    }

    @Nested
    @DisplayName("create() 수량 검증 실패 테스트")
    class Create_Quantity_Fail {

        @Test
        @DisplayName("수량이 null 이면 AppException(INVALID_QUANTITY) 를 던진다")
        void create_withNullQuantity_throwsException() {
            // when / then
            assertThatThrownBy(() -> OrderItem.create(MENU_ID, MENU_NAME, null, UNIT_PRICE))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.INVALID_QUANTITY);
                    });
        }

        @ParameterizedTest(name = "수량 = {0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("수량이 0 이하이면 AppException(INVALID_QUANTITY) 를 던진다")
        void create_withNonPositiveQuantity_throwsException(int invalidQuantity) {
            // when / then
            assertThatThrownBy(() -> OrderItem.create(MENU_ID, MENU_NAME, invalidQuantity, UNIT_PRICE))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.INVALID_QUANTITY);
                    });
        }

    }


    @Nested
    @DisplayName("create() 단가 검증 실패 테스트")
    class Create_UnitPrice_Fail {
        @Test
        @DisplayName("단가가 null 이면 AppException(TOTAL_PRICE_OVER_ZERO) 를 던진다")
        void create_withNullUnitPrice_throwsException() {
            // when / then
            assertThatThrownBy(() -> OrderItem.create(MENU_ID, MENU_NAME, QUANTITY, null))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.TOTAL_PRICE_OVER_ZERO);
                    });
        }

        @ParameterizedTest(name = "단가 = {0}")
        @ValueSource(ints = {-1, -1000})
        @DisplayName("단가가 음수이면 AppException(TOTAL_PRICE_OVER_ZERO) 를 던진다")
        void create_withNegativeUnitPrice_throwsException(int invalidPrice) {
            // when / then
            assertThatThrownBy(() -> OrderItem.create(MENU_ID, MENU_NAME, QUANTITY, invalidPrice))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getErrorCode()).isEqualTo(OrderErrorCode.TOTAL_PRICE_OVER_ZERO);
                    });
        }
    }

    @Nested
    @DisplayName("getSubTotal() 소계 계산")
    class GetSubTotal {

        @Test
        @DisplayName("소계 = 단가 × 수량 으로 계산된다")
        void getSubTotal_returnsUnitPriceMultipliedByQuantity() {
            // given
            OrderItem item = OrderItem.create(MENU_ID, MENU_NAME, 3, 10000);

            // when
            int subTotal = item.getSubTotal();

            // then
            assertThat(subTotal).isEqualTo(30000);
        }

        @Test
        @DisplayName("단가가 0 원이면 소계는 0 이다")
        void getSubTotal_withZeroUnitPrice_returnsZero() {
            // given
            OrderItem item = OrderItem.create(MENU_ID, MENU_NAME, 5, 0);

            // when / then
            assertThat(item.getSubTotal()).isZero();
        }

        @Test
        @DisplayName("수량이 1 개이면 소계 = 단가 와 동일하다")
        void getSubTotal_withQuantityOne_equalsUnitPrice() {
            // given
            OrderItem item = OrderItem.create(MENU_ID, MENU_NAME, 1, 25000);

            // when / then
            assertThat(item.getSubTotal()).isEqualTo(25000);
        }
    }

}
