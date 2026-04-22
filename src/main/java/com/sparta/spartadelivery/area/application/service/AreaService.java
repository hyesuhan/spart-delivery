package com.sparta.spartadelivery.area.application.service;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaUpdateRequest;
import com.sparta.spartadelivery.area.presentation.dto.response.AreaDetailResponse;
import com.sparta.spartadelivery.area.presentation.dto.response.AreaPageResponse;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
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
public class AreaService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "name",
            "city",
            "district",
            "createdAt",
            "updatedAt"
    );
    private static final String DEFAULT_SORT = "createdAt,DESC";

    private final AreaRepository areaRepository;

    @Transactional
    public AreaDetailResponse createArea(AreaCreateRequest request, UserPrincipal requester) {
        // 운영 지역 등록은 MANAGER, MASTER 권한만 허용한다.
        validateCreatePermission(requester);

        String name = request.name().strip();
        String city = request.city().strip();
        String district = request.district().strip();

        // soft delete 되지 않은 운영 지역 중 같은 이름이 있으면 등록을 막는다.
        validateDuplicateName(name);

        Area area = Area.builder()
                .name(name)
                .city(city)
                .district(district)
                .active(request.active())
                .build();

        return AreaDetailResponse.from(areaRepository.save(area));
    }

    public AreaPageResponse getAreas(int page, int size, String sort, Boolean active) {
        String normalizedSort = normalizeSort(sort);
        Pageable pageable = createPageable(page, size, normalizedSort);
        return AreaPageResponse.from(findAreas(active, pageable), normalizedSort);
    }

    public AreaDetailResponse getArea(UUID areaId) {
        Area area = getActiveArea(areaId);
        return AreaDetailResponse.from(area);
    }

    @Transactional
    public AreaDetailResponse updateArea(UUID areaId, AreaUpdateRequest request, UserPrincipal requester) {
        // 운영 지역 수정은 MANAGER, MASTER 권한만 허용한다.
        validateUpdatePermission(requester);

        Area area = getActiveArea(areaId);

        String name = request.name().strip();
        String city = request.city().strip();
        String district = request.district().strip();

        // 지역명이 실제로 변경된 경우에만 중복 검증을 수행한다.
        if (!area.getName().equals(name)) {
            validateDuplicateName(name);
        }

        area.update(name, city, district, request.active());
        return AreaDetailResponse.from(area);
    }

    @Transactional
    public void deleteArea(UUID areaId, UserPrincipal requester) {
        // 운영 지역 삭제는 MASTER 권한만 허용한다.
        validateDeletePermission(requester);

        // 이미 삭제된 운영 지역은 삭제 대상으로 보지 않는다.
        Area area = getActiveArea(areaId);

        // 실제 row를 제거하지 않고 감사 필드에 삭제 시각과 삭제자를 기록한다.
        area.markDeleted(requester.getAccountName());
    }

    private Area getActiveArea(UUID areaId) {
        return areaRepository.findByIdAndDeletedAtIsNull(areaId)
                .orElseThrow(() -> new AppException(AreaErrorCode.AREA_NOT_FOUND));
    }

    private void validateCreatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(AreaErrorCode.AREA_CREATE_ACCESS_DENIED);
    }

    private void validateUpdatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED);
    }

    private void validateDeletePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(AreaErrorCode.AREA_DELETE_ACCESS_DENIED);
    }

    private void validateDuplicateName(String name) {
        if (areaRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new AppException(AreaErrorCode.DUPLICATE_AREA_NAME);
        }
    }

    private org.springframework.data.domain.Page<Area> findAreas(Boolean active, Pageable pageable) {
        if (active == null) {
            return areaRepository.findAllByDeletedAtIsNull(pageable);
        }
        return areaRepository.findAllByDeletedAtIsNullAndActive(active, pageable);
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return DEFAULT_SORT;
        }

        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new AppException(AreaErrorCode.AREA_LIST_INVALID_SORT_FORMAT);
        }

        String property = parts[0].trim();
        String direction = parts[1].trim().toUpperCase();

        if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
            throw new AppException(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_PROPERTY);
        }
        if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new AppException(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_DIRECTION);
        }

        return property + "," + direction;
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new AppException(AreaErrorCode.AREA_LIST_INVALID_PAGE_NUMBER);
        }
        if (!ALLOWED_PAGE_SIZES.contains(size)) {
            throw new AppException(AreaErrorCode.AREA_LIST_INVALID_PAGE_SIZE);
        }

        String[] parts = sort.split(",");
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
    }
}
