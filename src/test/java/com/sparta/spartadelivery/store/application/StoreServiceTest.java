package com.sparta.spartadelivery.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.area.domain.repository.AreaRepository;
import com.sparta.spartadelivery.area.exception.AreaErrorCode;
import com.sparta.spartadelivery.auth.exception.AuthErrorCode;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.store.application.service.StoreService;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.store.presentation.dto.request.StoreCreateRequest;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
import java.util.Optional;
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
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @Mock
    private AreaRepository areaRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("OWNER는 가게를 등록할 수 있다")
    void createStoreByOwner() {
        UUID storeCategoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        StoreCreateRequest request = new StoreCreateRequest(
                storeCategoryId,
                areaId,
                " 스파르타 분식 ",
                " 서울특별시 강남구 테헤란로 123 ",
                " 02-1234-5678 "
        );
        UserPrincipal requester = principal(Role.OWNER);
        UserEntity owner = ownerEntity(1L);
        StoreCategory storeCategory = storeCategory(storeCategoryId);
        Area area = area(areaId);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(owner));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId)).thenReturn(Optional.of(storeCategory));
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.of(area));
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            ReflectionTestUtils.setField(store, "id", UUID.randomUUID());
            return store;
        });

        var response = storeService.createStore(request, requester);

        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeRepository).save(captor.capture());
        Store savedStore = captor.getValue();
        assertThat(savedStore.getOwner()).isEqualTo(owner);
        assertThat(savedStore.getStoreCategory()).isEqualTo(storeCategory);
        assertThat(savedStore.getArea()).isEqualTo(area);
        assertThat(savedStore.getName()).isEqualTo("스파르타 분식");
        assertThat(savedStore.getAddress()).isEqualTo("서울특별시 강남구 테헤란로 123");
        assertThat(savedStore.getPhone()).isEqualTo("02-1234-5678");
        assertThat(response.ownerId()).isEqualTo(owner.getId());
        assertThat(response.storeCategoryId()).isEqualTo(storeCategoryId);
        assertThat(response.areaId()).isEqualTo(areaId);
        assertThat(response.name()).isEqualTo("스파르타 분식");
    }

    @Test
    @DisplayName("OWNER가 아닌 사용자는 가게를 등록할 수 없다")
    void createStoreByNonOwnerDenied() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );
        UserPrincipal requester = principal(Role.CUSTOMER);

        assertThatThrownBy(() -> storeService.createStore(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_CREATE_OWNER_ROLE_REQUIRED);

        verify(userRepository, never()).findByIdAndDeletedAtIsNull(any());
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("요청자 사용자가 없으면 가게를 등록할 수 없다")
    void createStoreWhenOwnerNotFound() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );
        UserPrincipal requester = principal(Role.OWNER);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("가게 카테고리가 없으면 가게를 등록할 수 없다")
    void createStoreWhenStoreCategoryNotFound() {
        UUID storeCategoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        StoreCreateRequest request = new StoreCreateRequest(
                storeCategoryId,
                areaId,
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );
        UserPrincipal requester = principal(Role.OWNER);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ownerEntity(1L)));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("지역이 없으면 가게를 등록할 수 없다")
    void createStoreWhenAreaNotFound() {
        UUID storeCategoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        StoreCreateRequest request = new StoreCreateRequest(
                storeCategoryId,
                areaId,
                "스파르타 분식",
                "서울특별시 강남구 테헤란로 123",
                "02-1234-5678"
        );
        UserPrincipal requester = principal(Role.OWNER);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ownerEntity(1L)));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId))
                .thenReturn(Optional.of(storeCategory(storeCategoryId)));
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    private UserPrincipal principal(Role role) {
        return UserPrincipal.builder()
                .id(1L)
                .accountName("owner01")
                .email("owner01@example.com")
                .password("password")
                .nickname("점주01")
                .role(role)
                .build();
    }

    private UserEntity ownerEntity(Long id) {
        UserEntity owner = UserEntity.builder()
                .username("owner01")
                .nickname("점주01")
                .email("owner01@example.com")
                .password("password")
                .role(Role.OWNER)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(owner, "id", id);
        return owner;
    }

    private StoreCategory storeCategory(UUID id) {
        StoreCategory storeCategory = StoreCategory.builder()
                .name("분식")
                .build();
        ReflectionTestUtils.setField(storeCategory, "id", id);
        return storeCategory;
    }

    private Area area(UUID id) {
        Area area = Area.builder()
                .name("강남")
                .city("서울특별시")
                .district("강남구")
                .active(true)
                .build();
        ReflectionTestUtils.setField(area, "id", id);
        return area;
    }
}
