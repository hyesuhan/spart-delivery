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
import com.sparta.spartadelivery.store.presentation.dto.request.StoreUpdateRequest;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import com.sparta.spartadelivery.user.domain.repository.UserRepository;
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
                " 서울 강남구 테헤란로 123 ",
                " 02-1234-5678 "
        );
        UserPrincipal requester = principal(1L, Role.OWNER, "owner01");
        UserEntity owner = ownerEntity(1L, "owner01");
        StoreCategory storeCategory = storeCategory(storeCategoryId, "분식");
        Area area = area(areaId, "강남");

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
        assertThat(savedStore.getName()).isEqualTo("스파르타 분식");
        assertThat(savedStore.getAddress()).isEqualTo("서울 강남구 테헤란로 123");
        assertThat(savedStore.getPhone()).isEqualTo("02-1234-5678");
        assertThat(response.ownerId()).isEqualTo(owner.getId());
        assertThat(response.storeCategoryId()).isEqualTo(storeCategoryId);
        assertThat(response.areaId()).isEqualTo(areaId);
    }

    @Test
    @DisplayName("OWNER가 아니면 가게를 등록할 수 없다")
    void createStoreByNonOwnerDenied() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울 강남구 테헤란로 123",
                "02-1234-5678"
        );

        assertThatThrownBy(() -> storeService.createStore(request, principal(1L, Role.CUSTOMER, "customer01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_CREATE_OWNER_ROLE_REQUIRED);

        verify(userRepository, never()).findByIdAndDeletedAtIsNull(any());
        verify(storeRepository, never()).save(any());
    }

    @Test
    @DisplayName("요청 사용자가 없으면 가게를 등록할 수 없다")
    void createStoreWhenOwnerNotFound() {
        StoreCreateRequest request = new StoreCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 분식",
                "서울 강남구 테헤란로 123",
                "02-1234-5678"
        );
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, principal(1L, Role.OWNER, "owner01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("가게 카테고리가 없으면 가게를 등록할 수 없다")
    void createStoreWhenStoreCategoryNotFound() {
        UUID storeCategoryId = UUID.randomUUID();
        StoreCreateRequest request = new StoreCreateRequest(
                storeCategoryId,
                UUID.randomUUID(),
                "스파르타 분식",
                "서울 강남구 테헤란로 123",
                "02-1234-5678"
        );

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ownerEntity(1L, "owner01")));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, principal(1L, Role.OWNER, "owner01")))
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
                "서울 강남구 테헤란로 123",
                "02-1234-5678"
        );

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(ownerEntity(1L, "owner01")));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId))
                .thenReturn(Optional.of(storeCategory(storeCategoryId, "분식")));
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.createStore(request, principal(1L, Role.OWNER, "owner01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_FOUND);
    }

    @Test
    @DisplayName("OWNER는 본인 가게를 수정할 수 있다")
    void updateStoreByOwner() {
        UUID storeId = UUID.randomUUID();
        UUID storeCategoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        Store store = store(storeId, 1L, "이전 가게", "분식", "강남");
        StoreUpdateRequest request = new StoreUpdateRequest(
                storeCategoryId,
                areaId,
                " 스파르타 떡볶이 ",
                " 서울 송파구 올림픽로 321 ",
                " 02-9876-5432 "
        );

        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId))
                .thenReturn(Optional.of(storeCategory(storeCategoryId, "분식")));
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId))
                .thenReturn(Optional.of(area(areaId, "송파")));

        var response = storeService.updateStore(storeId, request, principal(1L, Role.OWNER, "owner01"));

        assertThat(store.getName()).isEqualTo("스파르타 떡볶이");
        assertThat(store.getAddress()).isEqualTo("서울 송파구 올림픽로 321");
        assertThat(store.getPhone()).isEqualTo("02-9876-5432");
        assertThat(response.name()).isEqualTo("스파르타 떡볶이");
    }

    @Test
    @DisplayName("MANAGER는 가게를 수정할 수 있다")
    void updateStoreByManager() {
        UUID storeId = UUID.randomUUID();
        UUID storeCategoryId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        Store store = store(storeId, 1L, "이전 가게", "분식", "강남");
        StoreUpdateRequest request = new StoreUpdateRequest(
                storeCategoryId,
                areaId,
                "스파르타 떡볶이",
                "서울 송파구 올림픽로 321",
                "02-9876-5432"
        );

        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));
        when(storeCategoryRepository.findByIdAndDeletedAtIsNull(storeCategoryId))
                .thenReturn(Optional.of(storeCategory(storeCategoryId, "분식")));
        when(areaRepository.findByIdAndDeletedAtIsNull(areaId))
                .thenReturn(Optional.of(area(areaId, "송파")));

        var response = storeService.updateStore(storeId, request, principal(2L, Role.MANAGER, "manager01"));

        assertThat(response.name()).isEqualTo("스파르타 떡볶이");
        assertThat(store.getOwner().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("본인 가게가 아닌 OWNER는 수정할 수 없다")
    void updateStoreByAnotherOwnerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "이전 가게", "분식", "강남");
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울 송파구 올림픽로 321",
                "02-9876-5432"
        );

        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.updateStore(storeId, request, principal(2L, Role.OWNER, "owner02")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_UPDATE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("CUSTOMER는 가게를 수정할 수 없다")
    void updateStoreByCustomerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "이전 가게", "분식", "강남");
        StoreUpdateRequest request = new StoreUpdateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "스파르타 떡볶이",
                "서울 송파구 올림픽로 321",
                "02-9876-5432"
        );

        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.updateStore(storeId, request, principal(3L, Role.CUSTOMER, "customer01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_UPDATE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("OWNER는 본인 가게를 숨김 처리할 수 있다")
    void hideStoreByOwner() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        var response = storeService.hideStore(storeId, principal(1L, Role.OWNER, "owner01"));

        assertThat(store.isHidden()).isTrue();
        assertThat(response.hidden()).isTrue();
    }

    @Test
    @DisplayName("MANAGER는 가게를 숨김 처리할 수 있다")
    void hideStoreByManager() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        var response = storeService.hideStore(storeId, principal(2L, Role.MANAGER, "manager01"));

        assertThat(store.isHidden()).isTrue();
        assertThat(response.hidden()).isTrue();
    }

    @Test
    @DisplayName("본인 가게가 아닌 OWNER는 숨김 처리할 수 없다")
    void hideStoreByAnotherOwnerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.hideStore(storeId, principal(2L, Role.OWNER, "owner02")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_HIDE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("CUSTOMER는 가게를 숨김 처리할 수 없다")
    void hideStoreByCustomerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.hideStore(storeId, principal(3L, Role.CUSTOMER, "customer01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_HIDE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("OWNER는 본인 가게를 삭제할 수 있다")
    void deleteStoreByOwner() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        storeService.deleteStore(storeId, principal(1L, Role.OWNER, "owner01"));

        assertThat(store.isDeleted()).isTrue();
        assertThat(store.getDeletedBy()).isEqualTo("owner01");
        assertThat(store.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("MASTER는 가게를 삭제할 수 있다")
    void deleteStoreByMaster() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        storeService.deleteStore(storeId, principal(9L, Role.MASTER, "master01"));

        assertThat(store.isDeleted()).isTrue();
        assertThat(store.getDeletedBy()).isEqualTo("master01");
    }

    @Test
    @DisplayName("본인 가게가 아닌 OWNER는 삭제할 수 없다")
    void deleteStoreByAnotherOwnerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.deleteStore(storeId, principal(2L, Role.OWNER, "owner02")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_DELETE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("CUSTOMER는 가게를 삭제할 수 없다")
    void deleteStoreByCustomerDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> storeService.deleteStore(storeId, principal(3L, Role.CUSTOMER, "customer01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_DELETE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("삭제 대상 가게가 없으면 예외가 발생한다")
    void deleteStoreWhenNotFound() {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findByIdAndDeletedAtIsNull(storeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.deleteStore(storeId, principal(1L, Role.OWNER, "owner01")))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("공개 가게 상세 정보를 조회할 수 있다")
    void getStore() {
        UUID storeId = UUID.randomUUID();
        Store store = store(storeId, 1L, "스파르타 분식", "분식", "강남");
        when(storeRepository.findByIdAndDeletedAtIsNullAndIsHiddenFalse(storeId))
                .thenReturn(Optional.of(store));

        var response = storeService.getStore(storeId);

        assertThat(response.name()).isEqualTo("스파르타 분식");
        assertThat(response.hidden()).isFalse();
    }

    @Test
    @DisplayName("공개 가게가 아니면 상세 조회할 수 없다")
    void getStoreWhenNotFound() {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findByIdAndDeletedAtIsNullAndIsHiddenFalse(storeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeService.getStore(storeId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("일반 가게 목록은 숨김 가게를 제외하고 조회한다")
    void getStores() {
        Store first = store(UUID.randomUUID(), 1L, "스파르타 분식", "분식", "강남");
        Store second = store(UUID.randomUUID(), 1L, "스파르타 치킨", "치킨", "송파");
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllPublicStores(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        var response = storeService.getStores(0, 10, null);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).name()).isEqualTo("스파르타 분식");
        assertThat(response.content().get(1).name()).isEqualTo("스파르타 치킨");
    }

    @Test
    @DisplayName("일반 가게 목록 결과가 없으면 빈 목록을 반환한다")
    void getStoresWithEmptyContent() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllPublicStores(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = storeService.getStores(0, 10, null);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    @DisplayName("관리자 목록은 hidden=false면 숨김 가게를 제외한다")
    void getAdminStoresWithoutHidden() {
        Store visibleStore = store(UUID.randomUUID(), 1L, "스파르타 분식", "분식", "강남");
        UserPrincipal requester = principal(2L, Role.MANAGER, "manager01");
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllPublicStores(pageable))
                .thenReturn(new PageImpl<>(List.of(visibleStore), pageable, 1));

        var response = storeService.getAdminStores(requester, 0, 10, null, false);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).hidden()).isFalse();
    }

    @Test
    @DisplayName("관리자 목록은 hidden=true면 숨김 가게를 포함한다")
    void getAdminStoresWithHidden() {
        Store visibleStore = store(UUID.randomUUID(), 1L, "스파르타 분식", "분식", "강남");
        Store hiddenStore = hiddenStore(UUID.randomUUID(), 1L, "스파르타 치킨", "치킨", "송파");
        UserPrincipal requester = principal(2L, Role.MANAGER, "manager01");
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(visibleStore, hiddenStore), pageable, 2));

        var response = storeService.getAdminStores(requester, 0, 10, null, true);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(1).hidden()).isTrue();
    }

    @Test
    @DisplayName("관리자 권한이 없으면 관리자 목록을 조회할 수 없다")
    void getAdminStoresByCustomerDenied() {
        UserPrincipal requester = principal(1L, Role.CUSTOMER, "customer01");

        assertThatThrownBy(() -> storeService.getAdminStores(requester, 0, 10, null, true))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_ADMIN_LIST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("페이지 번호가 0보다 작으면 목록을 조회할 수 없다")
    void getStoresWithInvalidPageNumber() {
        assertThatThrownBy(() -> storeService.getStores(-1, 10, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_LIST_INVALID_PAGE_NUMBER);
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 목록을 조회할 수 없다")
    void getStoresWithInvalidPageSize() {
        assertThatThrownBy(() -> storeService.getStores(0, 20, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_LIST_INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("정렬 조건 형식이 올바르지 않으면 목록을 조회할 수 없다")
    void getStoresWithInvalidSortFormat() {
        assertThatThrownBy(() -> storeService.getStores(0, 10, "createdAt"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_LIST_INVALID_SORT_FORMAT);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 필드면 목록을 조회할 수 없다")
    void getStoresWithUnsupportedSortProperty() {
        assertThatThrownBy(() -> storeService.getStores(0, 10, "id,DESC"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_LIST_UNSUPPORTED_SORT_PROPERTY);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방향이면 목록을 조회할 수 없다")
    void getStoresWithUnsupportedSortDirection() {
        assertThatThrownBy(() -> storeService.getStores(0, 10, "createdAt,DOWN"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreErrorCode.STORE_LIST_UNSUPPORTED_SORT_DIRECTION);
    }

    private UserPrincipal principal(Long id, Role role, String accountName) {
        return UserPrincipal.builder()
                .id(id)
                .accountName(accountName)
                .email(accountName + "@example.com")
                .password("password")
                .nickname(accountName)
                .role(role)
                .build();
    }

    private UserEntity ownerEntity(Long id, String username) {
        UserEntity owner = UserEntity.builder()
                .username(username)
                .nickname(username)
                .email(username + "@example.com")
                .password("password")
                .role(Role.OWNER)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(owner, "id", id);
        return owner;
    }

    private StoreCategory storeCategory(UUID id, String name) {
        StoreCategory storeCategory = StoreCategory.builder()
                .name(name)
                .build();
        ReflectionTestUtils.setField(storeCategory, "id", id);
        return storeCategory;
    }

    private Area area(UUID id, String name) {
        Area area = Area.builder()
                .name(name)
                .city("서울특별시")
                .district("강남구")
                .active(true)
                .build();
        ReflectionTestUtils.setField(area, "id", id);
        return area;
    }

    private Store store(UUID id, Long ownerId, String name, String categoryName, String areaName) {
        UserEntity owner = ownerEntity(ownerId, "owner" + ownerId);
        StoreCategory storeCategory = storeCategory(UUID.randomUUID(), categoryName);
        Area area = area(UUID.randomUUID(), areaName);

        Store store = Store.builder()
                .owner(owner)
                .storeCategory(storeCategory)
                .area(area)
                .name(name)
                .address("서울 강남구 테헤란로 123")
                .phone("02-1234-5678")
                .build();
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }

    private Store hiddenStore(UUID id, Long ownerId, String name, String categoryName, String areaName) {
        Store store = store(id, ownerId, name, categoryName, areaName);
        store.hide();
        return store;
    }
}
