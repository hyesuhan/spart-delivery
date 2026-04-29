package com.sparta.spartadelivery.store.presentation.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StoreCreateRequest validation test")
class StoreCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 요청이면 위반이 없다")
    void validRequest_NoViolations() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
