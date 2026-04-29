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
import com.sparta.spartadelivery.store.presentation.dto.response.StorePageResponse;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
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

    public StorePageResponse getStores(int page, int size, String sort) {
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);

        return StorePageResponse.from(
                storeRepository.findAllPublicStores(pageable),
                normalizedSort
        );
    }

    public StoreDetailResponse getStore(UUID storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNullAndIsHiddenFalse(storeId)
                .orElseThrow(() -> new AppException(StoreErrorCode.STORE_NOT_FOUND));

        return StoreDetailResponse.from(store);
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

    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }

        String normalizedPhone = phone.strip();
        return normalizedPhone.isEmpty() ? null : normalizedPhone;
    }
}
