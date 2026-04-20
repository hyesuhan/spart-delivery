package com.sparta.spartadelivery.user.application.service;

import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.USER_ID;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.assertAppException;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.createUser;
import static com.sparta.spartadelivery.user.application.service.UserServiceTestFixture.principal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceDeleteTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CUSTOMER", "OWNER", "MANAGER"})
    @DisplayName("CUSTOMER, OWNER, MANAGER는 본인 계정을 탈퇴할 수 있다")
    void deleteMeByAnyRole(Role requesterRole) {
        UserEntity user = createUser(USER_ID, requesterRole);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteMe(principal(USER_ID, requesterRole));

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getDeletedBy()).isEqualTo("requester");
    }

    @Test
    @DisplayName("MASTER는 본인 계정을 탈퇴할 수 없다")
    void deleteMeByMasterDenied() {
        UserEntity user = createUser(USER_ID, Role.MASTER);
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(user));

        assertAppException(
                () -> userService.deleteMe(principal(USER_ID, Role.MASTER)),
                UserErrorCode.MASTER_DELETE_DENIED
        );
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getDeletedBy()).isNull();
    }

    @Test
    @DisplayName("본인 탈퇴 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다")
    void deleteMeWithMissingUser() {
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.empty());

        assertAppException(
                () -> userService.deleteMe(principal(USER_ID, Role.CUSTOMER)),
                AuthErrorCode.USER_NOT_FOUND
        );
    }
}
