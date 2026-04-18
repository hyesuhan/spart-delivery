package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    private void validateAdminUpdateFields(ReqUpdateUserDto request) {
        // MANAGER와 MASTER는 사용자의 비밀번호를 수정할 수 없다.
        if (request.hasPasswordUpdate()) {
            throw new AppException(UserErrorCode.MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD);
        }
    }

    private boolean canManagerUpdate(UserEntity targetUser) {
        return targetUser.getRole() == Role.CUSTOMER || targetUser.getRole() == Role.OWNER;
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
