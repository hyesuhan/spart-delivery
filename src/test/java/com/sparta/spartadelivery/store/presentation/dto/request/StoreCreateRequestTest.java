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

    @Test
    @DisplayName("storeCategoryId가 null이면 위반이 발생한다")
    void storeCategoryId_Null_ViolationOccurs() {
        StoreCreateRequest request = new StoreCreateRequest(
                null,
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("가게 카테고리 ID는 필수입니다.");
    }

    @Test
    @DisplayName("areaId가 null이면 위반이 발생한다")
    void areaId_Null_ViolationOccurs() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                null,
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("지역 ID는 필수입니다.");
    }

    @Test
    @DisplayName("name이 공백이면 위반이 발생한다")
    void name_Blank_ViolationOccurs() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "   ",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("가게명은 필수입니다.");
    }

    @Test
    @DisplayName("address가 공백이면 위반이 발생한다")
    void address_Blank_ViolationOccurs() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "   ",
                "02-1234-5678"
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("가게 주소는 필수입니다.");
    }

    @Test
    @DisplayName("phone은 null이어도 위반이 없다")
    void phone_Null_NoViolation() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                null
        );

        Set<ConstraintViolation<StoreCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
