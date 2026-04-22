package com.sparta.spartadelivery.storecategory.application.service;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.storecategory.presentation.dto.response.StoreCategoryDetailResponse;
import com.sparta.spartadelivery.user.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreCategoryService {

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
}
