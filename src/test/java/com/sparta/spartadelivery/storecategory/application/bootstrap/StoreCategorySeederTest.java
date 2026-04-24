package com.sparta.spartadelivery.storecategory.application.bootstrap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import com.sparta.spartadelivery.storecategory.application.config.StoreCategorySeedProperties;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import java.util.List;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class StoreCategorySeederTest {

    @Mock
    private StoreCategoryRepository storeCategoryRepository;

    private ArgumentCaptor<Iterable<StoreCategory>> categoriesCaptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        categoriesCaptor = ArgumentCaptor.forClass(Iterable.class);
    }

    @Test
    @DisplayName("시드 데이터가 활성화되어 있으면 없는 카테고리만 저장한다")
    void seedStoreCategories() throws Exception {
        StoreCategorySeedProperties seedProperties = new StoreCategorySeedProperties(
                true,
                List.of("한식", " 중식 ", "한식", "", "일식")
        );
        StoreCategorySeeder seeder = new StoreCategorySeeder(storeCategoryRepository, seedProperties);

        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("한식")).thenReturn(true);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("중식")).thenReturn(false);
        when(storeCategoryRepository.existsByNameAndDeletedAtIsNull("일식")).thenReturn(false);

        seeder.run(new DefaultApplicationArguments(new String[0]));

        verify(storeCategoryRepository).saveAll(categoriesCaptor.capture());
        List<String> savedNames = toNames(categoriesCaptor.getValue());
        assertThat(savedNames).containsExactly("중식", "일식");
    }

    @Test
    @DisplayName("시드 데이터가 비활성화되어 있으면 저장하지 않는다")
    void skipWhenSeedDisabled() throws Exception {
        StoreCategorySeedProperties seedProperties = new StoreCategorySeedProperties(
                false,
                List.of("한식", "중식")
        );
        StoreCategorySeeder seeder = new StoreCategorySeeder(storeCategoryRepository, seedProperties);

        seeder.run(new DefaultApplicationArguments(new String[0]));

        verify(storeCategoryRepository, never()).existsByNameAndDeletedAtIsNull(any());
        verify(storeCategoryRepository, never()).saveAll(any());
    }

    private List<String> toNames(Iterable<StoreCategory> categories) {
        return StreamSupport.stream(categories.spliterator(), false)
                .map(StoreCategory::getName)
                .toList();
    }
}
