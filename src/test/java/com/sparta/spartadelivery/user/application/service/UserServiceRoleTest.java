package com.sparta.spartadelivery.user.application.service;

import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.MANAGER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.USER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.assertAppException;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.principal;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.roleUpdateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceRoleTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("MASTER는 다른 사용자의 권한을 요청 권한으로 수정할 수 있다")
    void updateUserRoleByMaster(Role newRole) {
        UserEntity targetUser = givenUser(Role.CUSTOMER);
        ReqUpdateUserRoleDto request = roleUpdateRequest(newRole);

        var response = userService.updateUserRole(USER_ID, request, principal(MANAGER_ID, Role.MASTER));

        assertThat(targetUser.getRole()).isEqualTo(newRole);
        assertThat(response.id()).isEqualTo(USER_ID);
        assertThat(response.username()).isEqualTo("user01");
        assertThat(response.role()).isEqualTo(newRole);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER", "MANAGER"})
    @DisplayName("MASTER가 아닌 사용자는 사용자 권한을 수정할 수 없다")
    void updateUserRoleByNonMasterDenied(Role requesterRole) {
        givenUser(Role.CUSTOMER);
        ReqUpdateUserRoleDto request = roleUpdateRequest(Role.OWNER);

        assertAppException(
                () -> userService.updateUserRole(USER_ID, request, principal(MANAGER_ID, requesterRole)),
                UserErrorCode.USER_ROLE_UPDATE_ACCESS_DENIED
        );
    }

    @Test
    @DisplayName("MASTER는 자기 자신의 권한을 수정할 수 없다")
    void updateOwnRoleByMasterDenied() {
        givenUser(Role.MASTER);
        ReqUpdateUserRoleDto request = roleUpdateRequest(Role.OWNER);

        assertAppException(
                () -> userService.updateUserRole(USER_ID, request, principal(USER_ID, Role.MASTER)),
                UserErrorCode.SELF_ROLE_UPDATE_DENIED
        );
    }

    @Test
    @DisplayName("권한 수정 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void updateUserRoleWithMissingUser() {
        ReqUpdateUserRoleDto request = roleUpdateRequest(Role.OWNER);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.updateUserRole(USER_ID, request, principal(MANAGER_ID, Role.MASTER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }

    private UserEntity givenUser(Role role) {
        UserEntity user = UserServiceTestFixture.createUser(USER_ID, role);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        return user;
    }
}
