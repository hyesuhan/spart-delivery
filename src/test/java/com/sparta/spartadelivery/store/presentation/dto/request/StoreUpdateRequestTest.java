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

@DisplayName("StoreUpdateRequest validation test")
class StoreUpdateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 수정 요청이면 위반이 없다")
    void validRequest_NoViolations() {
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울특별시 서초구 강남대로 321",
                "02-9876-5432"
        );

        Set<ConstraintViolation<StoreUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("storeCategoryId가 null이면 위반이 발생한다")
    void storeCategoryId_Null_ViolationOccurs() {
        StoreUpdateRequest request = new StoreUpdateRequest(
                null,
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울특별시 서초구 강남대로 321",
                "02-9876-5432"
        );

        Set<ConstraintViolation<StoreUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("가게 카테고리 ID는 필수입니다.");
    }

    @Test
    @DisplayName("name이 공백이면 위반이 발생한다")
    void name_Blank_ViolationOccurs() {
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                "서울특별시 서초구 강남대로 321",
                "02-9876-5432"
        );

        Set<ConstraintViolation<StoreUpdateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("가게명은 필수입니다.");
    }
}
