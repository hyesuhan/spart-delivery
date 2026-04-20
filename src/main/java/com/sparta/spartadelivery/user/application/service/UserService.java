package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserDetailDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserPageDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserRoleDto;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "username",
            "nickname",
            "email",
            "role",
            "createdAt",
            "updatedAt"
    );
    private static final String DEFAULT_SORT = "createdAt,DESC";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 본인 상세 조회 API
    @Transactional(readOnly = true)
    public ResUserDetailDto getMe(UserPrincipal requester) {
        UserEntity user = getActiveUser(requester.getId());
        return ResUserDetailDto.from(user);
    }

    // 다른 사용자 상세 조회 API (MANAGER, MASTER만 사용 가능)
    @Transactional(readOnly = true)
    public ResUserDetailDto getUser(Long userId, UserPrincipal requester) {
        validateUserDetailPermission(requester);
        UserEntity user = getActiveUser(userId);
        return ResUserDetailDto.from(user);
    }

    // 본인 회원 탈퇴 API
    @Transactional
    public void deleteMe(UserPrincipal requester) {
        UserEntity user = getActiveUser(requester.getId());
        user.markDeleted(requester.getAccountName());
    }

    // 사용자 목록 조회 API
    @Transactional(readOnly = true)
    public ResUserPageDto getUsers(
            UserPrincipal requester,
            String keyword,
            Role role,
            int page,
            int size,
            String sort
    ) {
        validateUserListPermission(requester);
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);
        return ResUserPageDto.from(userRepository.searchUsers(normalizedKeyword, role, pageable), normalizedSort);
    }

    // 본인 정보 수정 API (CUSTOMER, OWNER, MANAGER, MASTER 모두 사용 가능)
    @Transactional
    public ResUpdateUserDto updateMe(ReqUpdateUserDto request, UserPrincipal requester) {
        // 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다.
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(requester.getId())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        validateDuplicateEmail(request, user);
        user.updateProfile(request.username(), request.nickname(), request.email(), request.isPublic());
        if (request.hasPasswordUpdate()) {
            user.updatePassword(passwordEncoder.encode(request.password()));
        }
        return ResUpdateUserDto.from(user);
    }

    // 사용자의 정보 수정 API (MANAGER, MASTER만 사용 가능)
    @Transactional
    public ResUpdateUserDto updateUser(Long userId, ReqUpdateUserDto request, UserPrincipal requester) {
        // 대상 사용자가 없으면 USER_NOT_FOUND로 처리한다.
        UserEntity targetUser = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        validateManagePermission(requester, targetUser);
        validateAdminUpdateFields(request);
        validateDuplicateEmail(request, targetUser);

        targetUser.updateProfile(request.username(), request.nickname(), request.email(), request.isPublic());

        return ResUpdateUserDto.from(targetUser);
    }

    // MASTER 전용 사용자 권한 수정 API
    @Transactional
    public ResUpdateUserRoleDto updateUserRole(Long userId, ReqUpdateUserRoleDto request, UserPrincipal requester) {
        // 권한 수정의 대상이 되는 사용자를 조회하여 존재하지 않으면 USER_NOT_FOUND로 처리한다.
        UserEntity targetUser = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        validateRoleUpdatePermission(requester, targetUser);
        targetUser.updateRole(request.role());

        return ResUpdateUserRoleDto.from(targetUser);
    }

    private void validateManagePermission(UserPrincipal requester, UserEntity targetUser) {
        // MASTER는 모든 사용자 정보를 수정할 수 있도록 한다
        if (requester.getRole() == Role.MASTER) {
            return;
        }
        if (requester.getRole() == Role.MANAGER) {
            // MANAGER는 CUSTOMER, OWNER 사용자의 정보만 수정할 수 있도록 한다.
            if (canManagerUpdate(targetUser)) {
                return;
            }
            throw new AppException(UserErrorCode.MANAGER_TARGET_ACCESS_DENIED);
        } // MANAGER 또는 MASTER가 아닌 일반 사용자는 다른 사용자의 정보를 수정할 권한이 없다.
        throw new AppException(UserErrorCode.USER_UPDATE_ACCESS_DENIED);
    }


    // 사용자 목록 조회 요청 시 MANAGER, MASTER 권한만 조회 가능하도록 접근 제어 적용
    private void validateUserListPermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(UserErrorCode.USER_LIST_ACCESS_DENIED);
    }

    private void validateUserDetailPermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(UserErrorCode.USER_DETAIL_ACCESS_DENIED);
    }

    private UserEntity getActiveUser(Long userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return DEFAULT_SORT;
        }
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new AppException(UserErrorCode.USER_LIST_INVALID_SORT_FORMAT);
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();
        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new AppException(UserErrorCode.USER_LIST_UNSUPPORTED_SORT_PROPERTY);
        }
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new AppException(UserErrorCode.USER_LIST_UNSUPPORTED_SORT_DIRECTION);
        }
        return property + "," + direction;
    }

    // 페이지네이션과 정렬 조건을 검증해 Pageable로 변환한다.
    private Pageable createPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new AppException(UserErrorCode.USER_LIST_INVALID_PAGE_NUMBER);
        }
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new AppException(UserErrorCode.USER_LIST_INVALID_PAGE_SIZE);
        }

        String[] parts = sort.split(",");
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
    }

    private void validateAdminUpdateFields(ReqUpdateUserDto request) {
        // MANAGER와 MASTER는 사용자의 비밀번호를 수정할 수 없다.
        if (request.hasPasswordUpdate()) {
            throw new AppException(UserErrorCode.MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD);
        }
    }

    private boolean canManagerUpdate(UserEntity targetUser) {
        return targetUser.getRole() == Role.CUSTOMER || targetUser.getRole() == Role.OWNER;
    }

    private void validateRoleUpdatePermission(UserPrincipal requester, UserEntity targetUser) {
        // MASTER만 다른 사용자의 권한을 수정할 수 있도록 한다.
        if (requester.getRole() != Role.MASTER) {
            throw new AppException(UserErrorCode.USER_ROLE_UPDATE_ACCESS_DENIED);
        }
        // MASTER가 자기 자신의 권한을 변경할 수 없도록 한다.
        if (requester.getId().equals(targetUser.getId())) {
            throw new AppException(UserErrorCode.SELF_ROLE_UPDATE_DENIED);
        }
    }

    // 이메일 변경 시 중복 이메일 검증을 수행한다.
    private void validateDuplicateEmail(ReqUpdateUserDto request, UserEntity targetUser) {
        // 회원 정보 수정 시 이메일을 변경하지 않거나, 이메일이 null인 경우에는 중복 체크를 하지 않는다.
        if (request.email() == null || request.email().equals(targetUser.getEmail())) {
            return;
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(AuthErrorCode.DUPLICATE_EMAIL);
        }
    }

}
