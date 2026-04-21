package com.sparta.spartadelivery.area.application.service;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.response.AreaDetailResponse;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaService {

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

    @Transactional
    public void deleteArea(UUID areaId, UserPrincipal requester) {
        // 운영 지역 삭제는 MASTER 권한만 허용한다.
        validateDeletePermission(requester);

        // 이미 삭제된 운영 지역은 삭제 대상으로 다루지 않는다.
        Area area = areaRepository.findByIdAndDeletedAtIsNull(areaId)
                .orElseThrow(() -> new AppException(AreaErrorCode.AREA_NOT_FOUND));

        // 실제 row를 삭제하지 않고 감사 필드에 삭제 시각과 삭제자를 기록한다.
        area.markDeleted(requester.getAccountName());
    }

    private void validateCreatePermission(UserPrincipal requester) {
        if (requester.getRole() == Role.MANAGER || requester.getRole() == Role.MASTER) {
            return;
        }
        throw new AppException(AreaErrorCode.AREA_CREATE_ACCESS_DENIED);
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
}
