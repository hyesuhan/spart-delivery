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
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        // when: 운영 지역 등록 서비스를 호출한다.
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

        // then: active 값이 생략되면 엔티티 기본값인 true로 등록된다.
        verify(areaRepository).save(any(Area.class));
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("운영 지역 등록 요청값은 중복 검증과 저장 전에 앞뒤 공백이 제거된다")
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

        // when & then: 권한 예외가 발생하고 중복 검증 및 저장은 수행되지 않는다.
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
