package com.sparta.spartadelivery.order.presentation.dto.request;

import com.sparta.spartadelivery.order.domain.entity.OrderType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderCreateRequest 검증 테스트 (Validation)")
public class OrderCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // 스프링 없이 Validator를 생성하는 순수 자바 방식
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("성공: 모든 필드가 유효할 때 검증에 통과한다.")
    void validate_Success() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderType.ONLINE,
                "문 앞에 놔주세요",
                List.of(new OrderItemRequest(UUID.randomUUID(), "치킨", 1, 20000))
        );

        // when
        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("실패: 필수 필드(storeId, addressId, orderType, orderItems)가 null이면 검증에 실패한다.")
    void validate_Fail_RequiredFieldsNull() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                null, // storeId
                null, // addressId
                null, // orderType
                "request",
                null  // orderItems
        );

        // when
        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        // then
        // 4개의 @NotNull 위반이 발생해야 함
        assertThat(violations).hasSize(4);

        // 특정 필드에서 에러가 났는지 상세 확인도 가능합니다.
        assertThat(violations).extracting("propertyPath").asString()
                .contains("storeId", "addressId", "orderType", "orderItems");
    }

    @Test
    @DisplayName("실패: 주문 항목(orderItems)이 null이면 검증에 실패한다.")
    void validate_Fail_OrderItemsNull() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OrderType.ONLINE,
                "request",
                null
        );

        // when
        Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("orderItem 은 존재해야 합니다.");
    }
}
