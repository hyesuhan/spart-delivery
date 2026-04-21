package com.sparta.spartadelivery.category.domain.repository;

import com.sparta.spartadelivery.category.domain.entity.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
