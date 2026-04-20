package com.sparta.spartadelivery.address.presentation.dto;

import com.sparta.spartadelivery.address.presentation.dto.request.AddressCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressCreatedRequest validation test")
public class AddressCreatedRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = (Validator) factory.getValidator();
    }

    @Test
    @DisplayName("유효한 요청 - 위반 없음")
    void validRequest_NoViolations() {
        // given
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "서울시 강남구",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("address가 null이면 위반 발생")
    void address_Null_ViolationOccurs() {
        // given
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                null,       // address = null
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

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
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "",         // address = ""
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

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
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "   ",      // address = 공백만
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

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
        AddressCreateRequest request = new AddressCreateRequest(
                null,       // alias = null (필수 아님)
                "서울시 강남구",
                "101호",
                "12345",
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("detail과 zipCode는 null이어도 위반 없음 - 선택 필드")
    void detail_ZipCode_Null_NoViolation() {
        // given
        AddressCreateRequest request = new AddressCreateRequest(
                "집",
                "서울시 강남구",
                null,       // detail = null (필수 아님)
                null,       // zipCode = null (필수 아님)
                false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("isDefault true/false 모두 유효")
    void isDefault_BothValues_NoViolation() {
        // given
        AddressCreateRequest defaultTrue = new AddressCreateRequest(
                "집", "서울시 강남구", "101호", "12345", true
        );
        AddressCreateRequest defaultFalse = new AddressCreateRequest(
                "회사", "서울시 서초구", "202호", "54321", false
        );

        // when
        Set<ConstraintViolation<AddressCreateRequest>> violationsTrue = validator.validate(defaultTrue);
        Set<ConstraintViolation<AddressCreateRequest>> violationsFalse = validator.validate(defaultFalse);

        // then
        assertThat(violationsTrue).isEmpty();
        assertThat(violationsFalse).isEmpty();
    }
}
