package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
}