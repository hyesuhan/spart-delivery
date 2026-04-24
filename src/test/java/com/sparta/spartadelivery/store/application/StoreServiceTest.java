package com.sparta.spartadelivery.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.store.application.service.StoreService;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.store.domain.repository.StoreRepository;
import com.sparta.spartadelivery.store.exception.StoreErrorCode;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("가게 목록을 페이지네이션 형태로 조회한다")
    void getStores() {
        Store first = store("스파르타 분식", "분식", "강남");
        Store second = store("스파르타 치킨", "치킨", "서초");
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        var response = storeService.getStores(0, 10, null);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).name()).isEqualTo("스파르타 분식");
        assertThat(response.content().get(0).storeCategoryName()).isEqualTo("분식");
        assertThat(response.content().get(0).areaName()).isEqualTo("강남");
        assertThat(response.content().get(1).name()).isEqualTo("스파르타 치킨");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.sort()).isEqualTo("createdAt,DESC");
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void getStoresWithEmptyContent() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(storeRepository.findAllByDeletedAtIsNull(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = storeService.getStores(0, 10, null);

        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
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

    private Store store(String name, String categoryName, String areaName) {
        UserEntity owner = UserEntity.builder()
                .username("owner01")
                .nickname("점주01")
                .email("owner01@example.com")
                .password("password")
                .role(Role.OWNER)
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);

        StoreCategory storeCategory = StoreCategory.builder()
                .name(categoryName)
                .build();
        ReflectionTestUtils.setField(storeCategory, "id", UUID.randomUUID());

        Area area = Area.builder()
                .name(areaName)
                .city("서울특별시")
                .district("강남구")
                .active(true)
                .build();
        ReflectionTestUtils.setField(area, "id", UUID.randomUUID());

        Store store = Store.builder()
                .owner(owner)
                .storeCategory(storeCategory)
                .area(area)
                .name(name)
                .address("서울특별시 강남구 테헤란로 123")
                .phone("02-1234-5678")
                .build();
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());
        return store;
    }
}
