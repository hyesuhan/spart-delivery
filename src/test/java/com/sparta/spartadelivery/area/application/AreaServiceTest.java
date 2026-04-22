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
        // given: MANAGER 권한 사용자가 중복되지 않은 운영 지역 등록을 요청한다.
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> {
            Area area = invocation.getArgument(0);
            ReflectionTestUtils.setField(area, "id", UUID.randomUUID());
            return area;
        });

        // when
        var response = areaService.createArea(request, requester);

        // then: Area 엔티티가 저장되고 등록 결과가 응답으로 반환된다.
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
        // given: MASTER 권한 사용자가 active 값을 생략하고 운영 지역 등록을 요청한다.
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", null);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var response = areaService.createArea(request, requester);

        // then: active 값이 생략되면 엔티티는 기본값인 true로 등록된다.
        verify(areaRepository).save(any(Area.class));
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("운영 지역 등록 요청값은 중복 검증과 저장 전에 앞뒤 공백을 제거한다")
    void createAreaWithTrimmedValues() {
        // given: 앞뒤 공백이 포함된 요청값을 준비한다.
        AreaCreateRequest request = new AreaCreateRequest(" Gwanghwamun ", " Seoul ", " Jongno-gu ", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(false);
        when(areaRepository.save(any(Area.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        areaService.createArea(request, requester);

        // then: 공백이 제거된 값으로 중복 검증과 저장이 수행된다.
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
        // given: CUSTOMER 권한 사용자가 운영 지역 등록을 요청한다.
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.CUSTOMER);

        // when & then: 권한 예외가 발생하고 중복 검증과 저장은 수행되지 않는다.
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
        // given: 삭제되지 않은 같은 이름의 운영 지역이 이미 존재한다.
        AreaCreateRequest request = new AreaCreateRequest("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.existsByNameAndDeletedAtIsNull("Gwanghwamun")).thenReturn(true);

        // when & then: 중복 예외가 발생하고 저장은 수행되지 않는다.
        assertThatThrownBy(() -> areaService.createArea(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.DUPLICATE_AREA_NAME);

        verify(areaRepository, never()).save(any());
    }

    @Test
    @DisplayName("운영 지역 목록을 페이지네이션으로 조회할 수 있다")
    void getAreas() {
        // given: 삭제되지 않은 운영 지역 목록이 조회된다.
        Area first = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        Area second = area("Jamsil", "Seoul", "Songpa-gu", true);
        when(areaRepository.findAllByDeletedAtIsNull(PageRequest.of(0, 10,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))))
                .thenReturn(new PageImpl<>(List.of(first, second),
                        PageRequest.of(0, 10,
                                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")),
                        2));

        // when
        var response = areaService.getAreas(0, 10, null);

        // then
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
        // given
        when(areaRepository.findAllByDeletedAtIsNull(PageRequest.of(0, 10,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))))
                .thenReturn(new PageImpl<>(List.of(),
                        PageRequest.of(0, 10,
                                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")),
                        0));

        // when
        var response = areaService.getAreas(0, 10, null);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    @DisplayName("페이지 번호가 0보다 작으면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidPageNumber() {
        assertThatThrownBy(() -> areaService.getAreas(-1, 10, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_PAGE_NUMBER);
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidPageSize() {
        assertThatThrownBy(() -> areaService.getAreas(0, 20, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("정렬 조건 형식이 올바르지 않으면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithInvalidSortFormat() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "createdAt"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_INVALID_SORT_FORMAT);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 필드면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithUnsupportedSortProperty() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "id,DESC"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_PROPERTY);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방향이면 운영 지역 목록을 조회할 수 없다")
    void getAreasWithUnsupportedSortDirection() {
        assertThatThrownBy(() -> areaService.getAreas(0, 10, "createdAt,DOWN"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_LIST_UNSUPPORTED_SORT_DIRECTION);
    }

    @Test
    @DisplayName("운영 지역 상세 정보를 조회할 수 있다")
    void getArea() {
        // given: 삭제되지 않은 운영 지역이 존재한다.
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        // when
        var response = areaService.getArea(areaId);

        // then
        assertThat(response.name()).isEqualTo("Gwanghwamun");
        assertThat(response.city()).isEqualTo("Seoul");
        assertThat(response.district()).isEqualTo("Jongno-gu");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("상세 조회 대상 운영 지역이 없으면 조회할 수 없다")
    void getAreaNotFound() {
        // given: areaId에 해당하는 미삭제 운영 지역이 없다.
        UUID areaId = UUID.randomUUID();
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> areaService.getArea(areaId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    @Test
    @DisplayName("MANAGER 권한 사용자는 운영 지역을 수정할 수 있다")
    void updateAreaByManager() {
        // given: 수정 대상 운영 지역과 MANAGER 권한 요청자를 준비한다.
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", false);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        // when
        var response = areaService.updateArea(areaId, request, requester);

        // then: 요청값으로 운영 지역 정보가 수정된다.
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
        // given
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        // when
        var response = areaService.updateArea(areaId, request, requester);

        // then
        assertThat(response.name()).isEqualTo("Jongno");
    }

    @Test
    @DisplayName("운영 지역 수정 요청값은 저장 전에 앞뒤 공백을 제거한다")
    void updateAreaWithTrimmedValues() {
        // given
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest(" Jongno ", " Seoul-si ", " Jongno-gu ", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(false);

        // when
        areaService.updateArea(areaId, request, requester);

        // then
        assertThat(area.getName()).isEqualTo("Jongno");
        assertThat(area.getCity()).isEqualTo("Seoul-si");
        assertThat(area.getDistrict()).isEqualTo("Jongno-gu");
    }

    @Test
    @DisplayName("운영 지역명이 변경되지 않으면 중복 검증 없이 수정할 수 있다")
    void updateAreaWithSameNameSkipsDuplicateCheck() {
        // given
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Gwanghwamun", "Seoul-si", "Jongno-gu", false);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        // when
        areaService.updateArea(areaId, request, requester);

        // then
        verify(areaRepository, never()).existsByNameAndDeletedAtIsNull(any());
        assertThat(area.getCity()).isEqualTo("Seoul-si");
        assertThat(area.isActive()).isFalse();
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 운영 지역을 수정할 수 없다")
    void updateAreaByCustomerDenied() {
        // given
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.CUSTOMER);

        // when & then
        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("OWNER 권한 사용자는 운영 지역을 수정할 수 없다")
    void updateAreaByOwnerDenied() {
        // given
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.OWNER);

        // when & then
        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_UPDATE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("수정할 운영 지역이 없으면 수정할 수 없다")
    void updateAreaNotFound() {
        // given
        UUID areaId = UUID.randomUUID();
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    @Test
    @DisplayName("변경하려는 운영 지역명이 중복되면 수정할 수 없다")
    void updateAreaWithDuplicateName() {
        // given
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        AreaUpdateRequest request = new AreaUpdateRequest("Jongno", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MANAGER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(areaRepository.existsByNameAndDeletedAtIsNull("Jongno")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> areaService.updateArea(areaId, request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.DUPLICATE_AREA_NAME);
    }

    @Test
    @DisplayName("MASTER 권한 사용자는 운영 지역을 삭제할 수 있다")
    void deleteAreaByMaster() {
        // given: 삭제되지 않은 운영 지역과 MASTER 권한 요청자를 준비한다.
        UUID areaId = UUID.randomUUID();
        Area area = area("Gwanghwamun", "Seoul", "Jongno-gu", true);
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));

        // when
        areaService.deleteArea(areaId, requester);

        // then: 삭제자와 삭제 시각이 기록된다.
        assertThat(area.isDeleted()).isTrue();
        assertThat(area.getDeletedBy()).isEqualTo("manager01");
    }

    @Test
    @DisplayName("MANAGER 권한 사용자는 운영 지역을 삭제할 수 없다")
    void deleteAreaByManagerDenied() {
        // given: MANAGER 권한 요청자를 준비한다.
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.MANAGER);

        // when & then: 권한 예외가 발생하고 운영 지역 조회는 수행되지 않는다.
        assertThatThrownBy(() -> areaService.deleteArea(areaId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_DELETE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 운영 지역을 삭제할 수 없다")
    void deleteAreaByCustomerDenied() {
        // given: CUSTOMER 권한 요청자를 준비한다.
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.CUSTOMER);

        // when & then: 권한 예외가 발생하고 운영 지역 조회는 수행되지 않는다.
        assertThatThrownBy(() -> areaService.deleteArea(areaId, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_DELETE_ACCESS_DENIED);

        verify(areaRepository, never()).findByIdAndDeletedAtIsNull(any());
    }

    @Test
    @DisplayName("삭제할 운영 지역이 없으면 삭제할 수 없다")
    void deleteAreaNotFound() {
        // given: areaId에 해당하는 미삭제 운영 지역이 없다.
        UUID areaId = UUID.randomUUID();
        UserPrincipal requester = principal(Role.MASTER);
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        // when & then
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
