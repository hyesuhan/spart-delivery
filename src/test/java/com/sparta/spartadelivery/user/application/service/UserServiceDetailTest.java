package com.sparta.spartadelivery.user.application.service;

import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.MANAGER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.USER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.assertAppException;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.createUser;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserDetailDto;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceDetailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("모든 권한은 본인 정보를 상세 조회할 수 있다")
    void getMeByAnyRole(Role requesterRole) {
        UserEntity user = createUser(USER_ID, requesterRole);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));

        var response = userService.getMe(principal(USER_ID, requesterRole));

        assertUserDetailResponse(response, user);
    }

    @ParameterizedTest
    @MethodSource("adminRequesterAndTargetRoles")
    @DisplayName("MANAGER와 MASTER는 모든 권한의 다른 사용자 정보를 상세 조회할 수 있다")
    void getUserByManagerOrMaster(Role requesterRole, Role targetRole) {
        UserEntity targetUser = createUser(USER_ID, targetRole);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(targetUser));

        var response = userService.getUser(USER_ID, principal(MANAGER_ID, requesterRole));

        assertUserDetailResponse(response, targetUser);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("CUSTOMER와 OWNER는 다른 사용자 정보를 상세 조회할 수 없다")
    void getUserByCustomerOrOwnerDenied(Role requesterRole) {
        assertAppException(
                () -> userService.getUser(USER_ID, principal(2L, requesterRole)),
                UserErrorCode.USER_DETAIL_ACCESS_DENIED
        );
        verify(userRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("본인 상세 조회 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void getMeWithMissingUser() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.getMe(principal(USER_ID, Role.CUSTOMER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    @DisplayName("다른 사용자 상세 조회 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void getUserWithMissingUser() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.getUser(USER_ID, principal(MANAGER_ID, Role.MANAGER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }

    private static Stream<Arguments> adminRequesterAndTargetRoles() {
        return Stream.of(Role.MANAGER, Role.MASTER)
                .flatMap(requesterRole -> Stream.of(Role.values())
                        .map(targetRole -> Arguments.of(requesterRole, targetRole)));
    }

    private void assertUserDetailResponse(ResUserDetailDto response, UserEntity user) {
        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.nickname()).isEqualTo(user.getNickname());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.role()).isEqualTo(user.getRole());
        assertThat(response.isPublic()).isEqualTo(user.isPublic());
        assertThat(response.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(user.getUpdatedAt());
    }
}
