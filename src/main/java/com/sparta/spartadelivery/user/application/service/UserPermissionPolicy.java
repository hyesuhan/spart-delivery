package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.request.ReqUpdateUserDto;
import org.springframework.stereotype.Component;

@Component
class UserPermissionPolicy {

    void validateUserListPermission(UserPrincipal requester) {
        if (isAdmin(requester)) {
            return;
        }
        throw new AppException(UserErrorCode.USER_LIST_ACCESS_DENIED);
    }

    void validateUserDetailPermission(UserPrincipal requester) {
        if (isAdmin(requester)) {
            return;
        }
        throw new AppException(UserErrorCode.USER_DETAIL_ACCESS_DENIED);
    }

    void validateManagePermission(UserPrincipal requester, UserEntity targetUser) {
        if (requester.getRole() == Role.MASTER) {
            return;
        }
        if (requester.getRole() == Role.MANAGER) {
            if (canManagerManage(targetUser)) {
                return;
            }
            throw new AppException(UserErrorCode.MANAGER_TARGET_ACCESS_DENIED);
        }
        throw new AppException(UserErrorCode.USER_UPDATE_ACCESS_DENIED);
    }

    void validateAdminUpdateFields(ReqUpdateUserDto request) {
        if (request.hasPasswordUpdate()) {
            throw new AppException(UserErrorCode.MANAGER_OR_MASTER_CAN_NOT_CHANGE_USERS_PASSWORD);
        }
    }

    void validateUserDeletePermission(UserPrincipal requester, UserEntity targetUser) {
        if (!isAdmin(requester)) {
            throw new AppException(UserErrorCode.USER_DELETE_ACCESS_DENIED);
        }
        if (requester.getId().equals(targetUser.getId())) {
            throw new AppException(UserErrorCode.SELF_DELETE_BY_ADMIN_API_DENIED);
        }
        validateMasterDeleteDenied(targetUser);
        if (requester.getRole() == Role.MANAGER && !canManagerManage(targetUser)) {
            throw new AppException(UserErrorCode.MANAGER_DELETE_TARGET_ACCESS_DENIED);
        }
    }

    void validateMasterDeleteDenied(UserEntity targetUser) {
        if (targetUser.getRole() == Role.MASTER) {
            throw new AppException(UserErrorCode.MASTER_DELETE_DENIED);
        }
    }

    void validateRoleUpdatePermission(UserPrincipal requester, UserEntity targetUser) {
        if (requester.getRole() != Role.MASTER) {
            throw new AppException(UserErrorCode.USER_ROLE_UPDATE_ACCESS_DENIED);
        }
        if (requester.getId().equals(targetUser.getId())) {
            throw new AppException(UserErrorCode.SELF_ROLE_UPDATE_DENIED);
        }
    }

    private boolean isAdmin(UserPrincipal requester) {
        return requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER;
    }

    private boolean canManagerManage(UserEntity targetUser) {
        return targetUser.getRole() == Role.CUSTOMER || targetUser.getRole() == Role.OWNER;
    }
}
