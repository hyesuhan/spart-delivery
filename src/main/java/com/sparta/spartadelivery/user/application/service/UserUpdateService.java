package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserReader userReader;
    private final UserPermissionPolicy userPermissionPolicy;

    @Transactional
    public ResUpdateUserDto updateMe(ReqUpdateUserDto request, UserPrincipal requester) {
        UserEntity user = userReader.getActiveUser(requester.getId());

        validateDuplicateEmail(request, user);
        user.updateProfile(request.username(), request.nickname(), request.email(), request.isPublic());
        if (request.hasPasswordUpdate()) {
            user.updatePassword(passwordEncoder.encode(request.password()));
        }
        return ResUpdateUserDto.from(user);
    }

    @Transactional
    public ResUpdateUserDto updateUser(Long userId, ReqUpdateUserDto request, UserPrincipal requester) {
        UserEntity targetUser = userReader.getActiveUser(userId);

        userPermissionPolicy.validateManagePermission(requester, targetUser);
        userPermissionPolicy.validateAdminUpdateFields(request);
        validateDuplicateEmail(request, targetUser);

        targetUser.updateProfile(request.username(), request.nickname(), request.email(), request.isPublic());

        return ResUpdateUserDto.from(targetUser);
    }

    private void validateDuplicateEmail(ReqUpdateUserDto request, UserEntity targetUser) {
        if (request.email() == null || request.email().equals(targetUser.getEmail())) {
            return;
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(AuthErrorCode.DUPLICATE_EMAIL);
        }
    }
}
