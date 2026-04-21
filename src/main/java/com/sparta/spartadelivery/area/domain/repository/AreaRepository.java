package com.sparta.spartadelivery.area.domain.repository;

import com.sparta.spartadelivery.area.domain.entity.Area;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, UUID> {
}
