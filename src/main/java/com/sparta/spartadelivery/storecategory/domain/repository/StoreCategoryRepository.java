package com.sparta.spartadelivery.storecategory.domain.repository;

import com.sparta.spartadelivery.storecategory.domain.entity.StoreCategory;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, UUID> {

    boolean existsByNameAndDeletedAtIsNull(String name);

    Page<StoreCategory> findAllByDeletedAtIsNull(Pageable pageable);
}
