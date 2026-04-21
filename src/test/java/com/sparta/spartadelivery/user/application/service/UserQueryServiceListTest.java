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

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceListTest {

    @Mock
    private UserRepository userRepository;

    private UserQueryService userQueryService;

    @BeforeEach
    void setUp() {
        userQueryService = new UserQueryService(
                userRepository,
                new UserReader(userRepository),
                new UserPermissionPolicy()
        );
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"MANAGER", "MASTER"})
    @DisplayName("MANAGER와 MASTER는 사용자 목록을 조회할 수 있다")
    void getUsersByManagerOrMaster(Role requesterRole) {
        UserEntity customer = createUser(1L, Role.CUSTOMER);
        UserEntity owner = createUser(2L, Role.OWNER);
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(userRepository.searchUsers(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(customer, owner), pageable, 42));

        var response = userQueryService.getUsers(principal(MANAGER_ID, requesterRole), null, null, 0, 10, "createdAt,DESC");

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
                () -> userQueryService.getUsers(principal(USER_ID, requesterRole), null, null, 0, 10, "createdAt,DESC"),
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

        var response = userQueryService.getUsers(
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

        var response = userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, null);

        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @Test
    @DisplayName("사용자 목록 조회 시 허용되지 않은 페이지 크기는 거부한다")
    void getUsersWithInvalidPageSize() {
        assertAppException(
                () -> userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 20, "createdAt,DESC"),
                UserErrorCode.USER_LIST_INVALID_PAGE_SIZE
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 음수 페이지 번호는 거부한다")
    void getUsersWithNegativePageNumber() {
        assertAppException(
                () -> userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, -1, 10, "createdAt,DESC"),
                UserErrorCode.USER_LIST_INVALID_PAGE_NUMBER
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 정렬 조건 형식이 잘못되면 거부한다")
    void getUsersWithInvalidSortFormat() {
        assertAppException(
                () -> userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "createdAt"),
                UserErrorCode.USER_LIST_INVALID_SORT_FORMAT
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 지원하지 않는 정렬 필드는 거부한다")
    void getUsersWithUnsupportedSortProperty() {
        assertAppException(
                () -> userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "password,DESC"),
                UserErrorCode.USER_LIST_UNSUPPORTED_SORT_PROPERTY
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 목록 조회 시 지원하지 않는 정렬 방향은 거부한다")
    void getUsersWithUnsupportedSortDirection() {
        assertAppException(
                () -> userQueryService.getUsers(principal(MANAGER_ID, Role.MANAGER), null, null, 0, 10, "createdAt,DOWN"),
                UserErrorCode.USER_LIST_UNSUPPORTED_SORT_DIRECTION
        );
        verify(userRepository, never()).searchUsers(any(), any(), any());
    }
}
