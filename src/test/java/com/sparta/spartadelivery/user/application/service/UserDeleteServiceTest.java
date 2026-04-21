package com.sparta.spartadelivery.user.application.service;

import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.MANAGER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.USER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.assertAppException;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.createUser;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserDeleteServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserDeleteService userDeleteService;

    @BeforeEach
    void setUp() {
        userDeleteService = new UserDeleteService(
                new UserReader(userRepository),
                new UserPermissionPolicy()
        );
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER", "MANAGER"})
    @DisplayName("CUSTOMER, OWNER, MANAGER는 본인 계정을 탈퇴할 수 있다")
    void deleteMeByAnyRole(Role requesterRole) {
        UserEntity user = givenActiveUser(USER_ID, requesterRole);
        UserPrincipal requester = principal(USER_ID, requesterRole);

        userDeleteService.deleteMe(requester);

        assertDeleted(user, requester.getAccountName());
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER", "MANAGER"})
    @DisplayName("MASTER는 CUSTOMER, OWNER, MANAGER 사용자를 탈퇴 처리할 수 있다")
    void deleteUserByMaster(Role targetRole) {
        UserEntity targetUser = givenActiveUser(USER_ID, targetRole);
        UserPrincipal requester = principal(MANAGER_ID, Role.MASTER);

        userDeleteService.deleteUser(USER_ID, requester);

        assertDeleted(targetUser, requester.getAccountName());
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("MANAGER는 CUSTOMER, OWNER 사용자를 탈퇴 처리할 수 있다")
    void deleteUserByManager(Role targetRole) {
        UserEntity targetUser = givenActiveUser(USER_ID, targetRole);
        UserPrincipal requester = principal(MANAGER_ID, Role.MANAGER);

        userDeleteService.deleteUser(USER_ID, requester);

        assertDeleted(targetUser, requester.getAccountName());
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @Test
    @DisplayName("MASTER는 본인 계정을 탈퇴할 수 없다")
    void deleteMeByMasterDenied() {
        UserEntity user = givenActiveUser(USER_ID, Role.MASTER);

        assertAppException(
                () -> userDeleteService.deleteMe(principal(USER_ID, Role.MASTER)),
                UserErrorCode.MASTER_DELETE_DENIED
        );
        assertNotDeleted(user);
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("CUSTOMER와 OWNER는 관리자용 사용자 탈퇴 API를 사용할 수 없다")
    void deleteUserByCustomerOrOwnerDenied(Role requesterRole) {
        UserEntity targetUser = givenActiveUser(USER_ID, Role.CUSTOMER);

        assertAppException(
                () -> userDeleteService.deleteUser(USER_ID, principal(MANAGER_ID, requesterRole)),
                UserErrorCode.USER_DELETE_ACCESS_DENIED
        );
        assertNotDeleted(targetUser);
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"MANAGER", "MASTER"})
    @DisplayName("관리자용 사용자 탈퇴 API로 자기 자신을 탈퇴 처리할 수 없다")
    void deleteUserBySelfDenied(Role requesterRole) {
        UserEntity targetUser = givenActiveUser(USER_ID, requesterRole);

        assertAppException(
                () -> userDeleteService.deleteUser(USER_ID, principal(USER_ID, requesterRole)),
                UserErrorCode.SELF_DELETE_BY_ADMIN_API_DENIED
        );
        assertNotDeleted(targetUser);
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @ParameterizedTest
    @MethodSource("adminRoles")
    @DisplayName("MASTER 계정은 관리자용 사용자 탈퇴 API로 탈퇴 처리할 수 없다")
    void deleteMasterByAdminDenied(Role requesterRole) {
        UserEntity targetUser = givenActiveUser(USER_ID, Role.MASTER);

        assertAppException(
                () -> userDeleteService.deleteUser(USER_ID, principal(MANAGER_ID, requesterRole)),
                UserErrorCode.MASTER_DELETE_DENIED
        );
        assertNotDeleted(targetUser);
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @Test
    @DisplayName("MANAGER는 MANAGER 사용자를 탈퇴 처리할 수 없다")
    void deleteManagerByManagerDenied() {
        UserEntity targetUser = givenActiveUser(USER_ID, Role.MANAGER);

        assertAppException(
                () -> userDeleteService.deleteUser(USER_ID, principal(MANAGER_ID, Role.MANAGER)),
                UserErrorCode.MANAGER_DELETE_TARGET_ACCESS_DENIED
        );
        assertNotDeleted(targetUser);
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @Test
    @DisplayName("본인 탈퇴 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void deleteMeWithMissingUser() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userDeleteService.deleteMe(principal(USER_ID, Role.CUSTOMER)),
                AuthErrorCode.USER_NOT_FOUND
        );
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    @Test
    @DisplayName("관리자 탈퇴 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void deleteUserWithMissingUser() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userDeleteService.deleteUser(USER_ID, principal(MANAGER_ID, Role.MASTER)),
                AuthErrorCode.USER_NOT_FOUND
        );
        verify(userRepository).findByIdAndDeletedAtIsNull(USER_ID);
    }

    private static Stream<Arguments> adminRoles() {
        return Stream.of(
                Arguments.of(Role.MANAGER),
                Arguments.of(Role.MASTER)
        );
    }

    private UserEntity givenActiveUser(Long userId, Role role) {
        UserEntity user = createUser(userId, role);
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        return user;
    }

    private void assertDeleted(UserEntity user, String deletedBy) {
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedBy()).isEqualTo(deletedBy);
    }

    private void assertNotDeleted(UserEntity user) {
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getDeletedBy()).isNull();
    }
}
