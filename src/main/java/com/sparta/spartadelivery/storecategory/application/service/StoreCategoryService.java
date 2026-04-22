package com.sparta.spartadelivery.storecategory.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryUpdateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryDetailResponse;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryPageResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreCategoryService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "name",
            "createdAt",
            "updatedAt"
    );
    private static final String DEFAULT_SORT = "createdAt,DESC";

    private final StoreCategoryRepository storeCategoryRepository;

    @Transactional
    public StoreCategoryDetailResponse createStoreCategory(
            StoreCategoryCreateRequest request,
            UserPrincipal requester
    ) {
        validateCreatePermission(requester);

        String name = request.name().strip();
        validateDuplicateName(name);

        StoreCategory storeCategory = StoreCategory.builder()
                .name(name)
                .build();

        return StoreCategoryDetailResponse.from(storeCategoryRepository.save(storeCategory));
    }

    public StoreCategoryPageResponse getStoreCategories(int page, int size, String sort) {
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);
        return StoreCategoryPageResponse.from(
                storeCategoryRepository.findAllByDeletedAtIsNull(pageable),
                normalizedSort
        );
    }

    public StoreCategoryDetailResponse getStoreCategory(UUID storeCategoryId) {
        StoreCategory storeCategory = getActiveStoreCategory(storeCategoryId);
        return StoreCategoryDetailResponse.from(storeCategory);
    }

    @Transactional
    public StoreCategoryDetailResponse updateStoreCategory(
            UUID storeCategoryId,
            StoreCategoryUpdateRequest request,
            UserPrincipal requester
    ) {
        validateUpdatePermission(requester);

        StoreCategory storeCategory = getActiveStoreCategory(storeCategoryId);
        String name = request.name().strip();

        if (!storeCategory.getName().equals(name)) {
            validateDuplicateName(name);
        }

        storeCategory.update(name);
        return StoreCategoryDetailResponse.from(storeCategory);
    }

    private void validateCreatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_CREATE_ACCESS_DENIED);
    }

    private void validateDuplicateName(String name) {
        if (storeCategoryRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new AppException(StoreCategoryErrorCode.DUPLICATE_STORE_CATEGORY_NAME);
        }
    }

    private void validateUpdatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_UPDATE_ACCESS_DENIED);
    }

    private StoreCategory getActiveStoreCategory(UUID storeCategoryId) {
        return storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId)
                .orElseThrow(() -> new AppException(StoreCategoryErrorCode.STORE_CATEGORY_NOT_FOUND));
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return DEFAULT_SORT;
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_SORT_FORMAT);
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();

        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_UNSUPPORTED_SORT_PROPERTY);
        }
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_UNSUPPORTED_SORT_DIRECTION);
        }

        return property + "," + direction;
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_PAGE_NUMBER);
        }
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new AppException(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_PAGE_SIZE);
        }

        String[] parts = sort.split(",");
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
    }
}
