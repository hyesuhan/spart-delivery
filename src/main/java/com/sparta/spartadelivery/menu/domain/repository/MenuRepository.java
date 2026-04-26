package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}
