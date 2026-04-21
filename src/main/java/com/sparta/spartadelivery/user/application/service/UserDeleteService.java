package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeleteService {

    private final UserReader userReader;
    private final UserPermissionPolicy userPermissionPolicy;

    @Transactional
    public void deleteMe(UserPrincipal requester) {
        UserEntity user = userReader.getActiveUser(requester.getId());
        userPermissionPolicy.validateMasterDeleteDenied(user);
        user.markDeleted(requester.getAccountName());
    }

    @Transactional
    public void deleteUser(Long userId, UserPrincipal requester) {
        UserEntity targetUser = userReader.getActiveUser(userId);
        userPermissionPolicy.validateUserDeletePermission(requester, targetUser);
        targetUser.markDeleted(requester.getAccountName());
    }
}
