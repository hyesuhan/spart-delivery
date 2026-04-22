package com.sparta.spartadelivery.area.domain.repository;

import com.sparta.spartadelivery.area.domain.entity.Area;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, UUID> {

    boolean existsByNameAndDeletedAtIsNull(String name);

    Optional<Area> findByIdAndDeletedAtIsNull(UUID id);

    Page<Area> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Area> findAllByDeletedAtIsNullAndActive(boolean active, Pageable pageable);
}
