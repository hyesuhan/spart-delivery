package com.sparta.spartadelivery.order.presentation.dto;

import com.sparta.spartadelivery.order.presentation.dto.request.OrderItemRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItemRequest 검증 테스트")
class OrderItemRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("성공: 모든 필드가 유효한 기준을 충족한다.")
    void validate_Success() {
        // given
        OrderItemRequest request = new OrderItemRequest(
                UUID.randomUUID(),
                "치즈 피자",
                2,
                15000
        );

        // when
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("실패: 메뉴 이름이 공백이거나 빈 문자열이면 검증에 실패한다.")
    void validate_Fail_MenuNameBlank() {
        // given
        OrderItemRequest request = new OrderItemRequest(
                UUID.randomUUID(),
                "   ", // Blank
                1,
                10000
        );

        // when
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").contains("메뉴 이름은 필수입니다.");
    }

    @Test
    @DisplayName("실패: 수량이나 단가가 음수이면 검증에 실패한다.")
    void validate_Fail_NegativeValues() {
        // given
        OrderItemRequest request = new OrderItemRequest(
                UUID.randomUUID(),
                "페퍼로니 피자",
                -1, // Min(0) 위반
                -500 // Min(0) 위반
        );

        // when
        Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting("message")
                .contains("단가는 0원 이상이어야 합니다.");
    }
}
