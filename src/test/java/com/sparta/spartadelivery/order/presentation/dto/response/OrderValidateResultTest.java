package com.sparta.spartadelivery.order.presentation.dto.response;


import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderValidateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("OrderValidateResult 단위 테스트")
public class OrderValidateResultTest {

    @Test
    @DisplayName("객체 생성 테스트")
    void createOrderValidateResult() {
        // given
        Order mockOrder = mock(Order.class);
        Address mockAddress = mock(Address.class);

        // when
        OrderValidateResult result = new OrderValidateResult(mockOrder, mockAddress);

        // then
        assertThat(result).isNotNull();
        assertThat(result.order()).isEqualTo(mockOrder);
        assertThat(result.address()).isEqualTo(mockAddress);
    }
}
