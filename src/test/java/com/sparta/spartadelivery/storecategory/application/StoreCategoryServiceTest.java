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
