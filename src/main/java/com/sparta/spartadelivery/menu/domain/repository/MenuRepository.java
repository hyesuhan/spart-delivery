package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // CUSTOMER: 숨김 X + 삭제 X
    List<Menu> findAllByStoreIdAndDeletedAtIsNullAndIsHiddenFalse(UUID storeId);

    // OWNER / MANAGER: 숨김 포함 + 삭제 X
    List<Menu> findAllByStoreIdAndDeletedAtIsNull(UUID storeId);

    // MASTER: 전체 조회
    List<Menu> findAllByStoreId(UUID storeId);

    // 상세 조회용
    Optional<Menu> findByIdAndDeletedAtIsNull(UUID menuId);
}