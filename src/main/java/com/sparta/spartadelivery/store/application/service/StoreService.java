package com.sparta.spartadelivery.store.application.service;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreCreateRequest;
import com.sparta.spartadelivery.store.presentation.dto.response.StoreDetailResponse;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final AreaRepository areaRepository;

    @Transactional
    public StoreDetailResponse createStore(StoreCreateRequest request, UserPrincipal requester) {
        validateCreatePermission(requester);

        UserEntity owner = userRepository.findByIdAndDeletedAtIsNull(requester.getId())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));
        StoreCategory storeCategory = storeCategoryRepository.findByIdAndDeletedAtIsNull(request.storeCategoryId())
                .orElseThrow(() -> new AppException(StoreCategoryErrorCode.STORE_CATEGORY_NOT_FOUND));
        Area area = areaRepository.findByIdAndDeletedAtIsNull(request.areaId())
                .orElseThrow(() -> new AppException(AreaErrorCode.AREA_NOT_FOUND));

        Store store = Store.builder()
                .owner(owner)
                .storeCategory(storeCategory)
                .area(area)
                .name(request.name().strip())
                .address(request.address().strip())
                .phone(normalizePhone(request.phone()))
                .build();

        return StoreDetailResponse.from(storeRepository.save(store));
    }

    private void validateCreatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.OWNER) {
            return;
        }
        if (requester.getRole() == Role.CUSTOMER
                || requester.getRole() == Role.MANAGER
                || requester.getRole() == Role.MASTER) {
            throw new AppException(StoreErrorCode.STORE_CREATE_OWNER_ROLE_REQUIRED);
        }
        throw new AppException(StoreErrorCode.STORE_CREATE_ACCESS_DENIED);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String normalizedPhone = phone.strip();
        return normalizedPhone.isEmpty() ? null : normalizedPhone;
    }
}
