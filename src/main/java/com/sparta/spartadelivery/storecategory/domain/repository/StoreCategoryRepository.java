package com.sparta.spartadelivery.storecategory.domain.repository;

import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, UUID> {

    boolean existsByNameAndDeletedAtIsNull(String name);
}
