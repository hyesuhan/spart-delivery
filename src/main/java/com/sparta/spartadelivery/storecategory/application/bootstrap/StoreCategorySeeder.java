package com.sparta.spartadelivery.storecategory.application.bootstrap;

import com.sparta.spartadelivery.storecategory.application.config.StoreCategorySeedProperties;
import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import com.sparta.spartadelivery.storecategory.domain.repository.StoreCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StoreCategorySeeder implements ApplicationRunner {

    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreCategorySeedProperties seedProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedProperties.enabled()) {
            return;
        }

        List<StoreCategory> categoriesToSave = seedProperties.values().stream()
                .map(String::strip)
                .filter(name -> !name.isBlank())
                .distinct()
                .filter(name -> !storeCategoryRepository.existsByNameAndDeletedAtIsNull(name))
                .map(name -> StoreCategory.builder()
                        .name(name)
                        .build())
                .toList();

        if (categoriesToSave.isEmpty()) {
            return;
        }

        storeCategoryRepository.saveAll(categoriesToSave);
    }
}
