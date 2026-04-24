package com.sparta.spartadelivery.store.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.store.presentation.dto.response.StorePageResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "name",
            "averageRating",
            "createdAt",
            "updatedAt"
    );
    private static final String DEFAULT_SORT = "createdAt,DESC";

    private final StoreRepository storeRepository;

    public StorePageResponse getStores(int page, int size, String sort) {
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);

        return StorePageResponse.from(
                storeRepository.findAllPublicStores(pageable),
                normalizedSort
        );
    }

    public StorePageResponse getAdminStores(
            UserPrincipal requester,
            int page,
            int size,
            String sort,
            boolean hidden
    ) {
        validateAdminListPermission(requester);

        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);

        return StorePageResponse.from(
                hidden
                        ? storeRepository.findAllByDeletedAtIsNull(pageable)
                        : storeRepository.findAllPublicStores(pageable),
                normalizedSort
        );
    }

    private void validateAdminListPermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(StoreErrorCode.STORE_ADMIN_LIST_ACCESS_DENIED);
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return DEFAULT_SORT;
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new AppException(StoreErrorCode.STORE_LIST_INVALID_SORT_FORMAT);
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();

        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new AppException(StoreErrorCode.STORE_LIST_UNSUPPORTED_SORT_PROPERTY);
        }
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new AppException(StoreErrorCode.STORE_LIST_UNSUPPORTED_SORT_DIRECTION);
        }

        return property + "," + direction;
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new AppException(StoreErrorCode.STORE_LIST_INVALID_PAGE_NUMBER);
        }
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new AppException(StoreErrorCode.STORE_LIST_INVALID_PAGE_SIZE);
        }

        String[] parts = sort.split(",");
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
    }
}
