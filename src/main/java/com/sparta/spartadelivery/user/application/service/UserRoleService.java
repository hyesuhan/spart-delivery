package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserRoleDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUpdateUserRoleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserReader userReader;
    private final UserPermissionPolicy userPermissionPolicy;

    @Transactional
    public ResUpdateUserRoleDto updateUserRole(Long userId, ReqUpdateUserRoleDto request, UserPrincipal requester) {
        UserEntity targetUser = userReader.getActiveUser(userId);

        userPermissionPolicy.validateRoleUpdatePermission(requester, targetUser);
        targetUser.updateRole(request.role());

        return ResUpdateUserRoleDto.from(targetUser);
    }
}
