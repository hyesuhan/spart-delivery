package com.sparta.spartadelivery.auth.presentation.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.spartadelivery.user.domain.entity.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("회원가입 요청이 입력 조건을 만족하면 검증을 통과한다")
    void validSignupRequest() {
        ReqSignupDto request = new ReqSignupDto(
                "User!",
                "Password1!",
                "유저01",
                "user01@example.com",
                Role.CUSTOMER
        );

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("회원가입 요청이 입력 조건을 만족하지 않으면 필드별 검증 메시지를 반환한다")
    void invalidSignupRequest() {
        ReqSignupDto request = new ReqSignupDto(
                "u",
                "password",
                "",
                "invalid-email",
                null
        );

        Set<String> messages = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toSet());

        assertThat(messages).contains(
                "username: 사용자 이름은 2~10자여야 합니다.",
                "password: 비밀번호는 8~15자의 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.",
                "nickname: 닉네임은 필수입니다.",
                "email: 이메일 형식이 올바르지 않습니다.",
                "role: 권한은 필수입니다."
        );
    }

    @Test
    @DisplayName("로그인 요청의 이메일과 비밀번호가 비어 있으면 검증 메시지를 반환한다")
    void invalidLoginRequest() {
        ReqLoginDto request = new ReqLoginDto("", "");

        Set<String> messages = validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toSet());

        assertThat(messages).contains(
                "email: 이메일은 필수입니다.",
                "password: 비밀번호는 필수입니다."
        );
    }
}
