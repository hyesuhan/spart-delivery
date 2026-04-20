package com.sparta.spartadelivery.user.application.service;

import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.MANAGER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.USER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.assertAppException;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.emailOnlyRequest;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.fullUpdateRequest;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.nicknameOnlyRequest;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.passwordOnlyRequest;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.principal;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.profileUpdateRequest;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.usernameOnlyRequest;
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
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
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
class UserServiceUpdateTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("본인은 프로필과 비밀번호를 수정할 수 있다")
    void updateMe() {
        UserEntity user = givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = fullUpdateRequest();
        when(userRepository.existsByEmail("user02@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");

        var response = userService.updateMe(request, principal(USER_ID, Role.CUSTOMER));

        assertUpdatedProfile(user);
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(response.username()).isEqualTo("user02");
        assertThat(response.nickname()).isEqualTo("유저02");
        assertThat(response.email()).isEqualTo("user02@example.com");
        assertThat(response.isPublic()).isFalse();
    }

    @Test
    @DisplayName("본인 수정 시 비밀번호가 없으면 기존 비밀번호를 유지한다")
    void updateMeWithoutPassword() {
        UserEntity user = givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = profileUpdateRequest();
        when(userRepository.existsByEmail("user02@example.com")).thenReturn(false);

        userService.updateMe(request, principal(USER_ID, Role.CUSTOMER));

        assertUpdatedProfile(user);
        assertThat(user.getPassword()).isEqualTo("old-password");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("PATCH 요청에서 null 필드는 기존 값을 유지한다")
    void updateMePartially() {
        UserEntity user = givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = nicknameOnlyRequest();

        userService.updateMe(request, principal(USER_ID, Role.CUSTOMER));

        assertThat(user.getUsername()).isEqualTo("user01");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getEmail()).isEqualTo("user01@example.com");
        assertThat(user.getPassword()).isEqualTo("old-password");
        assertThat(user.isPublic()).isTrue();
        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("MANAGER는 CUSTOMER와 OWNER 사용자 정보를 수정할 수 있다")
    void updateCustomerOrOwnerByManager(Role targetRole) {
        UserEntity targetUser = givenUser(targetRole);
        ReqUpdateUserDto request = profileUpdateRequest();
        when(userRepository.existsByEmail("user02@example.com")).thenReturn(false);

        var response = userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MANAGER));

        assertUpdatedProfile(targetUser);
        assertThat(targetUser.getPassword()).isEqualTo("old-password");
        assertThat(response.role()).isEqualTo(targetRole);
        verify(passwordEncoder, never()).encode(any());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("MASTER는 모든 사용자 정보를 수정할 수 있다")
    void updateAnyUserByMaster(Role targetRole) {
        UserEntity targetUser = givenUser(targetRole);
        ReqUpdateUserDto request = usernameOnlyRequest();

        var response = userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MASTER));

        assertThat(targetUser.getUsername()).isEqualTo("user02");
        assertThat(response.role()).isEqualTo(targetRole);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("CUSTOMER와 OWNER는 관리자 사용자 정보 수정 API를 사용할 수 없다")
    void updateUserByNonAdminDenied(Role requesterRole) {
        givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = usernameOnlyRequest();

        assertAppException(
                () -> userService.updateUser(USER_ID, request, principal(2L, requesterRole)),
                UserErrorCode.USER_UPDATE_ACCESS_DENIED
        );
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"MANAGER", "MASTER"})
    @DisplayName("MANAGER는 MANAGER와 MASTER 사용자 정보를 수정할 수 없다")
    void updateManagerOrMasterByManagerDenied(Role targetRole) {
        givenUser(targetRole);
        ReqUpdateUserDto request = usernameOnlyRequest();

        assertAppException(
                () -> userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MANAGER)),
                UserErrorCode.MANAGER_TARGET_ACCESS_DENIED
        );
    }

    @Test
    @DisplayName("관리자는 사용자 비밀번호를 수정할 수 없다")
    void updatePasswordByAdminDenied() {
        givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = passwordOnlyRequest();

        assertAppException(
                () -> userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MASTER)),
                UserErrorCode.MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD
        );
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("본인 수정 시 이미 사용 중인 이메일이면 거부한다")
    void updateMeWithDuplicateEmail() {
        givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = emailOnlyRequest("duplicate@example.com");
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertAppException(
                () -> userService.updateMe(request, principal(USER_ID, Role.CUSTOMER)),
                AuthErrorCode.DUPLICATE_EMAIL
        );
    }

    @Test
    @DisplayName("관리자 수정 시 이미 사용 중인 이메일이면 거부한다")
    void updateUserWithDuplicateEmail() {
        givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = emailOnlyRequest("duplicate@example.com");
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        assertAppException(
                () -> userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MASTER)),
                AuthErrorCode.DUPLICATE_EMAIL
        );
    }

    @Test
    @DisplayName("이메일이 기존 값과 같으면 중복 검증을 수행하지 않는다")
    void updateWithSameEmailDoesNotCheckDuplicateEmail() {
        UserEntity user = givenUser(Role.CUSTOMER);
        ReqUpdateUserDto request = emailOnlyRequest("user01@example.com");

        userService.updateMe(request, principal(USER_ID, Role.CUSTOMER));

        assertThat(user.getEmail()).isEqualTo("user01@example.com");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("본인 수정 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void updateMeWithMissingUser() {
        ReqUpdateUserDto request = usernameOnlyRequest();
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.updateMe(request, principal(USER_ID, Role.CUSTOMER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }

    @Test
    @DisplayName("관리자 수정 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void updateUserWithMissingUser() {
        ReqUpdateUserDto request = usernameOnlyRequest();
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.updateUser(USER_ID, request, principal(MANAGER_ID, Role.MANAGER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }

    private UserEntity givenUser(Role role) {
        UserEntity user = UserServiceTestFixture.createUser(USER_ID, role);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        return user;
    }

    private void assertUpdatedProfile(UserEntity user) {
        assertThat(user.getUsername()).isEqualTo("user02");
        assertThat(user.getNickname()).isEqualTo("유저02");
        assertThat(user.getEmail()).isEqualTo("user02@example.com");
        assertThat(user.isPublic()).isFalse();
    }
}
