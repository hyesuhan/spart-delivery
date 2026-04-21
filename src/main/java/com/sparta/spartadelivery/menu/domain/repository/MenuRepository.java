package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // Order 시 Menu의 현재 메뉴명과 가격 등의 정보가 필요합니다.
    List<Menu> findAllByIdIn(Collection<UUID> ids);

}
