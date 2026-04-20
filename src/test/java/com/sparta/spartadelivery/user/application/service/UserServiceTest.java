package com.sparta.spartadelivery.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.BaseErrorCode;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long MANAGER_ID = 99L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"MANAGER", "MASTER"})
    @DisplayName("MANAGER와 MASTER는 사용자 목록을 조회할 수 있다")
    void getUsersByManagerOrMaster(Role requesterRole) {
        UserEntity customer = createUser(1L, Role.CUSTOMER);
        UserEntity owner = createUser(2L, Role.OWNER);
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(userRepository.searchUsers(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(customer, owner), pageable, 42));

        var response = userService.getUsers(principal(MANAGER_ID, requesterRole), null, null, 0, 10, "createdAt,DESC");

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).id()).isEqualTo(1L);
        assertThat(response.content().get(0).username()).isEqualTo("user01");
        assertThat(response.content().get(0).email()).isEqualTo("user01@example.com");
        assertThat(response.content().get(0).role()).isEqualTo(Role.CUSTOMER);
        assertThat(response.content().get(0).isPublic()).isTrue();
        assertThat(response.content().get(1).id()).isEqualTo(2L);
        assertThat(response.content().get(1).role()).isEqualTo(Role.OWNER);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(42);
        assertThat(response.totalPages()).isEqualTo(5);
        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER"})
    @DisplayName("CUSTOMER와 OWNER는 사용자 목록을 조회할 수 없다")
    void getUsersByCustomerOrOwnerDenied(Role requesterRole) {
        assertAppException(
                () -> userService.getUsers(principal(USER_ID, requesterRole), null, null, 0, 10, "createdAt,DESC"),
                UserErrorCode.USER_LIST_ACCESS_DENIED
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 검색, 권한 필터, 페이지네이션, 정렬 조건을 적용한다")
    void getUsersWithSearchParameters() {
        var pageable = PageRequest.of(1, 30, Sort.by(Sort.Direction.ASC, "username"));
        when(userRepository.searchUsers("user", Role.CUSTOMER, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = userService.getUsers(
                principal(MANAGER_ID, Role.MANAGER),
                " user ",
                Role.CUSTOMER,
                1,
                30,
                "username,asc"
        );

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(30);
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
        assertThat(response.sort()).isEqualTo("username,ASC");
    }

    @Test
    @DisplayName("사용자 목록 조회 시 정렬 조건이 없으면 createdAt,DESC를 기본값으로 사용한다")
    void getUsersWithDefaultSort() {
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(userRepository.searchUsers(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, null);

        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @Test
    @DisplayName("사용자 목록 조회 시 허용되지 않은 페이지 크기는 거부한다")
    void getUsersWithInvalidPageSize() {
        assertAppException(
                () -> userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 20, "createdAt,DESC"),
                UserErrorCode.USER_LIST_INVALID_PAGE_SIZE
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 음수 페이지 번호는 거부한다")
    void getUsersWithNegativePageNumber() {
        assertAppException(
                () -> userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, -1, 10, "createdAt,DESC"),
                UserErrorCode.USER_LIST_INVALID_PAGE_NUMBER
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 정렬 조건 형식이 잘못되면 거부한다")
    void getUsersWithInvalidSortFormat() {
        assertAppException(
                () -> userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "createdAt"),
                UserErrorCode.USER_LIST_INVALID_SORT_FORMAT
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 지원하지 않는 정렬 필드는 거부한다")
    void getUsersWithUnsupportedSortProperty() {
        assertAppException(
                () -> userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "password,DESC"),
                UserErrorCode.USER_LIST_UNSUPPORTED_SORT_PROPERTY
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 지원하지 않는 정렬 방향은 거부한다")
    void getUsersWithUnsupportedSortDirection() {
        assertAppException(
                () -> userService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "createdAt,DOWN"),
                UserErrorCode.USER_LIST_UNSUPPORTED_SORT_DIRECTION
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

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
    @DisplayName("관리자 사용자 정보 수정 API에서는 비밀번호를 수정할 수 없다")
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
    @DisplayName("본인 수정 시 이미 사용 중인 이메일이면 수정을 거부한다")
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
    @DisplayName("관리자 수정 시 이미 사용 중인 이메일이면 수정을 거부한다")
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

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("MASTER는 다른 사용자의 권한을 요청한 권한으로 수정할 수 있다")
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
        UserEntity user = createUser(USER_ID, role);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));
        return user;
    }

    private ReqUpdateUserDto fullUpdateRequest() {
        return new ReqUpdateUserDto(
                "user02",
                "유저02",
                "user02@example.com",
                "Password1!",
                false
        );
    }

    private ReqUpdateUserDto profileUpdateRequest() {
        return new ReqUpdateUserDto(
                "user02",
                "유저02",
                "user02@example.com",
                null,
                false
        );
    }

    private ReqUpdateUserDto usernameOnlyRequest() {
        return new ReqUpdateUserDto("user02", null, null, null, null);
    }

    private ReqUpdateUserDto nicknameOnlyRequest() {
        return new ReqUpdateUserDto(null, "새닉네임", null, null, null);
    }

    private ReqUpdateUserDto passwordOnlyRequest() {
        return new ReqUpdateUserDto(null, null, null, "Password1!", null);
    }

    private ReqUpdateUserDto emailOnlyRequest(String email) {
        return new ReqUpdateUserDto(null, null, email, null, null);
    }

    private ReqUpdateUserRoleDto roleUpdateRequest(Role role) {
        return new ReqUpdateUserRoleDto(role);
    }

    private void assertUpdatedProfile(UserEntity user) {
        assertThat(user.getUsername()).isEqualTo("user02");
        assertThat(user.getNickname()).isEqualTo("유저02");
        assertThat(user.getEmail()).isEqualTo("user02@example.com");
        assertThat(user.isPublic()).isFalse();
    }

    private void assertAppException(ThrowingCallable callable, BaseErrorCode expectedErrorCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(expectedErrorCode);
    }

    private UserEntity createUser(Long id, Role role) {
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

    private String defaultUsername(Role role) {
        return switch (role) {
            case MANAGER -> "manager01";
            case MASTER -> "master01";
            case OWNER -> "owner01";
            case CUSTOMER -> "user01";
        };
    }

    private String defaultNickname(Role role) {
        return switch (role) {
            case MANAGER -> "매니저01";
            case MASTER -> "마스터01";
            case OWNER -> "점주01";
            case CUSTOMER -> "유저01";
        };
    }

    private String defaultEmail(Role role) {
        return defaultUsername(role) + "@example.com";
    }

    private UserPrincipal principal(Long id, Role role) {
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
