package com.sparta.spartadelivery.user.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import com.sparta.spartadelivery.user.exception.UserErrorCode;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserDetailDto;
import com.sparta.spartadelivery.user.presentation.dto.response.ResUserPageDto;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

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
    private final UserReader userReader;
    private final UserPermissionPolicy userPermissionPolicy;

    @Transactional(readOnly = true)
    public ResUserDetailDto getMe(UserPrincipal requester) {
        UserEntity user = userReader.getActiveUser(requester.getId());
        return ResUserDetailDto.from(user);
    }

    @Transactional(readOnly = true)
    public ResUserDetailDto getUser(Long userId, UserPrincipal requester) {
        userPermissionPolicy.validateUserDetailPermission(requester);
        UserEntity user = userReader.getActiveUser(userId);
        return ResUserDetailDto.from(user);
    }

    @Transactional(readOnly = true)
    public ResUserPageDto getUsers(
            UserPrincipal requester,
            String keyword,
            Role role,
            int page,
            int size,
            String sort
    ) {
        userPermissionPolicy.validateUserListPermission(requester);
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);
        return ResUserPageDto.from(userRepository.searchUsers(normalizedKeyword, role, pageable), normalizedSort);
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
}
