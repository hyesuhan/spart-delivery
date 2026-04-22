package com.sparta.spartadelivery.area.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.area.application.service.AreaService;
import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaCreateRequest;
import com.sparta.spartadelivery.area.presentation.dto.request.AreaUpdateRequest;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private AreaService areaService;

    @Test
    @DisplayName("MANAGER 권한 사용자는 운영 지역을 등록할 수 있다")
    void createAreaByManager() {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> {
            Area area = invocation.getArgument(0);
            ReflectionTestUtils.setField(area, "id", UUID.randomUUID());
            return area;
        });

        var response = areaService.createArea(request, requester);

        ArgumentCaptor<Area> areaCaptor = ArgumentCaptor.forClass(Area.class);
        verify(areaRepository).save(areaCaptor.capture());
        Area savedArea = areaCaptor.getValue();
        assertThat(savedArea.getName()).isEqualTo("Gwanghwamun");
        assertThat(savedArea.getCity()).isEqualTo("Seoul");
        assertThat(savedArea.getDistrict()).isEqualTo("Jongno-gu");
        assertThat(savedArea.isActive()).isTrue();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Gwanghwamun");
    }

    @Test
    @DisplayName("MASTER 권한 사용자는 운영 지역을 등록할 수 있다")
    void createAreaByMaster() {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", null);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = areaService.createArea(request, requester);

        verify(areaRepository).save(any(Area.class));
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("운영 지역 등록 요청값은 저장 전에 앞뒤 공백을 제거한다")
    void createAreaWithTrimmedValues() {
        AreaCreateRequest request = new AreaCreateRequest(" Gwanghwamun ", " Seoul ", " Jongno-gu ", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        areaService.createArea(request, requester);

        ArgumentCaptor<Area> areaCaptor = ArgumentCaptor.forClass(Area.class);
        verify(areaRepository).existsByNameAndDeletedAtIsNull("Gwanghwamun");
        verify(areaRepository).save(areaCaptor.capture());
        Area savedArea = areaCaptor.getValue();
        assertThat(savedArea.getName()).isEqualTo("Gwanghwamun");
        assertThat(savedArea.getCity()).isEqualTo("Seoul");
        assertThat(savedArea.getDistrict()).isEqualTo("Jongno-gu");
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 운영 지역을 등록할 수 없다")
    void createAreaByCustomerDenied() {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.CUSTOMER);

        assertThatThrownBy(() -> areaService.createArea(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_CREATE_ACCESS_DENIED);

        verify(areaRepository, never()).existsByNameAndDeletedAtIsNull(any());
        verify(areaRepository, never()).save(any());
    }

    @Test
    @DisplayName("삭제되지 않은 운영 지역명이 중복되면 등록할 수 없다")
    void createAreaWithDuplicateName() {
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(true);

        assertThatThrownBy(() -> areaService.createArea(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.DUPLICATE_AREA_NAME);

        verify(areaRepository, never()).save(any());
    }

    @Test
    @DisplayName("운영 지역 목록을 페이지네이션으로 조회할 수 있다")
    void getAreas() {
        Area first = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        Area second = area("Jamsil", "Seoul", "Songpa-gu", false);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(areaRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        var response = areaService.getAreas(0, 10, null, null);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).name()).isEqualTo("Gwanghwamun");
        assertThat(response.content().get(1).district()).isEqualTo("Songpa-gu");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @Test
    @DisplayName("운영 지역 목록 조회 결과가 없으면 빈 목록을 반환한다")
    void getAreasWithEmptyContent() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(areaRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = areaService.getAreas(0, 10, null, null);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    @DisplayName("활성 여부 조건으로 운영 지역 목록을 필터링해서 조회할 수 있다")
    void getAreasWithActiveFilter() {
        Area activeArea = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(areaRepository.findAllByDeletedAtIsNullAndActive(true, pageable))
                .thenReturn(new PageImpl<>(List.of(activeArea), pageable, 1));

        var response = areaService.getAreas(0, 10, null, true);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).active()).isTrue();
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지 번호가 0보다 작으면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidPageNumber() {
        assertThatThrownBy(() -> areaService.getAreas(-1, 10, null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_PAGE_NUMBER);
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidPageSize() {
        assertThatThrownBy(() -> areaService.getAreas(0, 20, null, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("정렬 조건 형식이 올바르지 않으면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidSortFormat() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "createdAt", null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_SORT_FORMAT);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 필드면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithUnsupportedSortProperty() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "active,DESC", null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_PROPERTY);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방향이면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithUnsupportedSortDirection() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "createdAt,DOWN", null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_DIRECTION);
    }

    @Test
    @DisplayName("운영 지역 상세 정보를 조회할 수 있다")
    void getArea() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        var response = areaService.getArea(areaId);

        assertThat(response.name()).isEqualTo("Gwanghwamun");
        assertThat(response.city()).isEqualTo("Seoul");
        assertThat(response.district()).isEqualTo("Jongno-gu");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("상세 조회 대상 운영 지역이 없으면 조회할 수 없다")
    void getAreaNotFound() {
        UUID areaId = UUID.randomUUID();
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areaService.getArea(areaId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    @Test
    @DisplayName("MANAGER 권한 사용자는 운영 지역을 수정할 수 있다")
    void updateAreaByManager() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", false);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        var response = areaService.updateArea(areaId, request, requester);

        assertThat(area.getName()).isEqualTo("Jongno");
        assertThat(area.getCity()).isEqualTo("Seoul");
        assertThat(area.getDistrict()).isEqualTo("Jongno-gu");
        assertThat(area.isActive()).isFalse();
        assertThat(response.name()).isEqualTo("Jongno");
        assertThat(response.active()).isFalse();
    }

    @Test
    @DisplayName("MASTER 권한 사용자는 운영 지역을 수정할 수 있다")
    void updateAreaByMaster() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        var response = areaService.updateArea(areaId, request, requester);

        assertThat(response.name()).isEqualTo("Jongno");
    }

    @Test
    @DisplayName("운영 지역 수정 요청값은 저장 전에 앞뒤 공백을 제거한다")
    void updateAreaWithTrimmedValues() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest(" Jongno ", " Seoul-si ", " Jongno-gu ", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        areaService.updateArea(areaId, request, requester);

        assertThat(area.getName()).isEqualTo("Jongno");
        assertThat(area.getCity()).isEqualTo("Seoul-si");
        assertThat(area.getDistrict()).isEqualTo("Jongno-gu");
    }

    @Test
    @DisplayName("운영 지역명이 변경되지 않으면 중복 검증 없이 수정할 수 있다")
    void updateAreaWithSameNameSkipsDuplicateCheck() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Gwanghwamun", "Seoul-si", "Jongno-gu", false);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        areaService.updateArea(areaId, request, requester);

        verify(areaRepository, never()).existsByNameAndDeletedAtIsNull(any());
        assertThat(area.getCity()).isEqualTo("Seoul-si");
        assertThat(area.isActive()).isFalse();
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 운영 지역을 수정할 수 없다")
    void updateAreaByCustomerDenied() {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.CUSTOMER);

        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("OWNER 권한 사용자는 운영 지역을 수정할 수 없다")
    void updateAreaByOwnerDenied() {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.OWNER);

        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("수정할 운영 지역이 없으면 수정할 수 없다")
    void updateAreaNotFound() {
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    @Test
    @DisplayName("변경하려는 운영 지역명이 중복되면 수정할 수 없다")
    void updateAreaWithDuplicateName() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(true);

        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.DUPLICATE_AREA_NAME);
    }

    @Test
    @DisplayName("MASTER 권한 사용자는 운영 지역을 삭제할 수 있다")
    void deleteAreaByMaster() {
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        areaService.deleteArea(areaId, requester);

        assertThat(area.isDeleted()).isTrue();
        assertThat(area.getDeletedBy()).isEqualTo("manager01");
    }

    @Test
    @DisplayName("MANAGER 권한 사용자는 운영 지역을 삭제할 수 없다")
    void deleteAreaByManagerDenied() {
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.MANAGER);

        assertThatThrownBy(() -> areaService.deleteArea(areaId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_DELETE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 운영 지역을 삭제할 수 없다")
    void deleteAreaByCustomerDenied() {
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.CUSTOMER);

        assertThatThrownBy(() -> areaService.deleteArea(areaId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_DELETE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("삭제할 운영 지역이 없으면 삭제할 수 없다")
    void deleteAreaNotFound() {
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areaService.deleteArea(areaId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    private Area area(String name, String city, String district, boolean active) {
        return Area.builder()
                .name(name)
                .city(city)
                .district(district)
                .active(active)
                .build();
    }

    private UserPrincipal principal(Role role) {
        return UserPrincipal.builder()
                .id(1L)
                .accountName("manager01")
                .email("manager01@example.com")
                .password("password")
                .nickname("manager")
                .role(role)
                .build();
    }
}
