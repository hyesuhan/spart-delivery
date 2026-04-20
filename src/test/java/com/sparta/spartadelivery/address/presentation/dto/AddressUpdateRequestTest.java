package com.sparta.spartadelivery.address.presentation.dto;

import com.sparta.spartadelivery.address.presentation.dto.request.AddressUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("AddressUpdateRequest validation test")
public class AddressUpdateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 요청 - 위반 없음")
    void validRequest_NoViolations() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "집",
                "서울시 강남구",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("address가 null이면 위반 발생")
    void address_Null_ViolationOccurs() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "집",
                null,
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("주소는 필수 입력사항 입니다.");
    }

    @Test
    @DisplayName("address가 빈 문자열이면 위반 발생")
    void address_Empty_ViolationOccurs() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "집",
                "",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("주소는 필수 입력사항 입니다.");
    }

    @Test
    @DisplayName("address가 공백만 있으면 위반 발생")
    void address_Whitespace_ViolationOccurs() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "집",
                "   ",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("주소는 필수 입력사항 입니다.");
    }

    @Test
    @DisplayName("alias는 null이어도 위반 없음 - 선택 필드")
    void alias_Null_NoViolation() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                null,
                "서울시 강남구",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("detail과 zipCode는 null이어도 위반 없음 - 선택 필드")
    void detail_ZipCode_Null_NoViolation() {
        // given
        AddressUpdateRequest request = new AddressUpdateRequest(
                "집",
                "서울시 강남구",
                null,
                null,
                false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("isDefault true/false 모두 유효")
    void isDefault_BothValues_NoViolation() {
        // given
        AddressUpdateRequest defaultTrue = new AddressUpdateRequest(
                "집", "서울시 강남구", "101호", "12345", true
        );
        AddressUpdateRequest defaultFalse = new AddressUpdateRequest(
                "회사", "서울시 서초구", "202호", "54321", false
        );

        // when
        Set<ConstraintViolation<AddressUpdateRequest>> violationsTrue = validator.validate(defaultTrue);
        Set<ConstraintViolation<AddressUpdateRequest>> violationsFalse = validator.validate(defaultFalse);

        // then
        assertThat(violationsTrue).isEmpty();
        assertThat(violationsFalse).isEmpty();
    }
}
