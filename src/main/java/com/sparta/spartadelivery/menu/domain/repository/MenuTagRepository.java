package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.MenuTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuTagRepository extends JpaRepository<MenuTag, UUID> {
}
