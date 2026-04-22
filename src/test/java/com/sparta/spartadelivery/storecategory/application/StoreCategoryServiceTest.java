package com.sparta.spartadelivery.storecategory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.infrastructure.config.security.UserPrincipal;
import com.sparta.spartadelivery.storecategory.application.service.StoreCategoryService;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import com.sparta.spartadelivery.storecategory.exception.StoreCategoryErrorCode;
import com.sparta.spartadelivery.storecategory.presentation.dto.request.StoreCategoryCreateRequest;
import com.sparta.spartadelivery.user.domain.entity.Role;
import java.util.List;
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
class StoreCategoryServiceTest {

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    @InjectMocks
    private StoreCategoryService storeCategoryService;

    @Test
    @DisplayName("MANAGER 권한 사용자는 가게 카테고리를 등록할 수 있다")
    void createStoreCategoryByManager() {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("한식");
        UserPrincipal requester = principal(Role.MANAGER);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(false);
        when(storeCategoryRepository.save(any(StoreCategory.class))).thenAnswer(invocation -> {
            StoreCategory storeCategory = invocation.getArgument(0);
            ReflectionTestUtils.setField(storeCategory, "id", UUID.randomUUID());
            return storeCategory;
        });

        var response = storeCategoryService.createStoreCategory(request, requester);

        ArgumentCaptor<StoreCategory> captor = ArgumentCaptor.forClass(StoreCategory.class);
        verify(storeCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("한식");
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("한식");
    }

    @Test
    @DisplayName("MASTER 권한 사용자는 가게 카테고리를 등록할 수 있다")
    void createStoreCategoryByMaster() {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("치킨");
        UserPrincipal requester = principal(Role.MASTER);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("치킨")).thenReturn(false);
        when(storeCategoryRepository.save(any(StoreCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = storeCategoryService.createStoreCategory(request, requester);

        verify(storeCategoryRepository).save(any(StoreCategory.class));
        assertThat(response.name()).isEqualTo("치킨");
    }

    @Test
    @DisplayName("가게 카테고리 등록 요청값은 저장 전에 앞뒤 공백을 제거한다")
    void createStoreCategoryWithTrimmedName() {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest(" 한식 ");
        UserPrincipal requester = principal(Role.MANAGER);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(false);
        when(storeCategoryRepository.save(any(StoreCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        storeCategoryService.createStoreCategory(request, requester);

        ArgumentCaptor<StoreCategory> captor = ArgumentCaptor.forClass(StoreCategory.class);
        verify(storeCategoryRepository).existsByNameAndDeletedAtIsNull("한식");
        verify(storeCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("CUSTOMER 권한 사용자는 가게 카테고리를 등록할 수 없다")
    void createStoreCategoryByCustomerDenied() {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("한식");
        UserPrincipal requester = principal(Role.CUSTOMER);

        assertThatThrownBy(() -> storeCategoryService.createStoreCategory(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_CREATE_ACCESS_DENIED);

        verify(storeCategoryRepository, never()).existsByNameAndDeletedAtIsNull(any());
        verify(storeCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("삭제되지 않은 가게 카테고리명이 중복되면 등록할 수 없다")
    void createStoreCategoryWithDuplicateName() {
        StoreCategoryCreateRequest request = new StoreCategoryCreateRequest("한식");
        UserPrincipal requester = principal(Role.MANAGER);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(true);

        assertThatThrownBy(() -> storeCategoryService.createStoreCategory(request, requester))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.DUPLICATE_STORE_CATEGORY_NAME);

        verify(storeCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("가게 카테고리 목록을 페이지네이션으로 조회할 수 있다")
    void getStoreCategories() {
        StoreCategory first = storeCategory("한식");
        StoreCategory second = storeCategory("치킨");
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeCategoryRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        var response = storeCategoryService.getStoreCategories(0, 10, null);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).name()).isEqualTo("한식");
        assertThat(response.content().get(1).name()).isEqualTo("치킨");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @Test
    @DisplayName("가게 카테고리 목록 조회 결과가 없으면 빈 목록을 반환한다")
    void getStoreCategoriesWithEmptyContent() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeCategoryRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = storeCategoryService.getStoreCategories(0, 10, null);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    @DisplayName("페이지 번호가 0보다 작으면 가게 카테고리 목록을 조회할 수 없다")
    void getStoreCategoriesWithInvalidPageNumber() {
        assertThatThrownBy(() -> storeCategoryService.getStoreCategories(-1, 10, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_PAGE_NUMBER);
    }

    @Test
    @DisplayName("페이지 크기가 허용 범위를 벗어나면 가게 카테고리 목록을 조회할 수 없다")
    void getStoreCategoriesWithInvalidPageSize() {
        assertThatThrownBy(() -> storeCategoryService.getStoreCategories(0, 20, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_PAGE_SIZE);
    }

    @Test
    @DisplayName("정렬 조건 형식이 올바르지 않으면 가게 카테고리 목록을 조회할 수 없다")
    void getStoreCategoriesWithInvalidSortFormat() {
        assertThatThrownBy(() -> storeCategoryService.getStoreCategories(0, 10, "createdAt"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_LIST_INVALID_SORT_FORMAT);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 필드면 가게 카테고리 목록을 조회할 수 없다")
    void getStoreCategoriesWithUnsupportedSortProperty() {
        assertThatThrownBy(() -> storeCategoryService.getStoreCategories(0, 10, "id,DESC"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_LIST_UNSUPPORTED_SORT_PROPERTY);
    }

    @Test
    @DisplayName("지원하지 않는 정렬 방향이면 가게 카테고리 목록을 조회할 수 없다")
    void getStoreCategoriesWithUnsupportedSortDirection() {
        assertThatThrownBy(() -> storeCategoryService.getStoreCategories(0, 10, "createdAt,DOWN"))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(StoreCategoryErrorCode.STORE_CATEGORY_LIST_UNSUPPORTED_SORT_DIRECTION);
    }

    private StoreCategory storeCategory(String name) {
        return StoreCategory.builder()
                .name(name)
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
