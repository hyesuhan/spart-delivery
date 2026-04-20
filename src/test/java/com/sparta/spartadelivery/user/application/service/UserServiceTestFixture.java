package com.sparta.spartadelivery.user.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.springframework.test.util.ReflectionTestUtils;

final class UserServiceTestFixture {

    static final Long USER_ID = 1L;
    static final Long MANAGER_ID = 99L;

    private UserServiceTestFixture() {
    }

    static ReqUpdateUserDto fullUpdateRequest() {
        return new ReqUpdateUserDto(
                "user02",
                "유저02",
                "user02@example.com",
                "Password1!",
                false
        );
    }

    static ReqUpdateUserDto profileUpdateRequest() {
        return new ReqUpdateUserDto(
                "user02",
                "유저02",
                "user02@example.com",
                null,
                false
        );
    }

    static ReqUpdateUserDto usernameOnlyRequest() {
        return new ReqUpdateUserDto("user02", null, null, null, null);
    }

    static ReqUpdateUserDto nicknameOnlyRequest() {
        return new ReqUpdateUserDto(null, "새닉네임", null, null, null);
    }

    static ReqUpdateUserDto passwordOnlyRequest() {
        return new ReqUpdateUserDto(null, null, null, "Password1!", null);
    }

    static ReqUpdateUserDto emailOnlyRequest(String email) {
        return new ReqUpdateUserDto(null, null, email, null, null);
    }

    static ReqUpdateUserRoleDto roleUpdateRequest(Role role) {
        return new ReqUpdateUserRoleDto(role);
    }

    static void assertAppException(ThrowingCallable callable, BaseErrorCode expectedErrorCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(expectedErrorCode);
    }

    static UserEntity createUser(Long id, Role role) {
        UserEntity user = UserEntity.builder()
                .username(defaultUsername(role))
                .nickname(defaultNickname(role))
                .email(defaultEmail(role))
                .password("old-password")
                .role(role)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    static String defaultUsername(Role role) {
        return switch (role) {
            case MANAGER -> "manager01";
            case MASTER -> "master01";
            case OWNER -> "owner01";
            case CUSTOMER -> "user01";
        };
    }

    static String defaultNickname(Role role) {
        return switch (role) {
            case MANAGER -> "매니저01";
            case MASTER -> "마스터01";
            case OWNER -> "점주01";
            case CUSTOMER -> "유저01";
        };
    }

    static String defaultEmail(Role role) {
        return defaultUsername(role) + "@example.com";
    }

    static UserPrincipal principal(Long id, Role role) {
        return UserPrincipal.builder()
                .id(id)
                .accountName("requester")
                .password("password")
                .nickname("요청자")
                .email("requester@example.com")
                .role(role)
                .build();
    }
}
