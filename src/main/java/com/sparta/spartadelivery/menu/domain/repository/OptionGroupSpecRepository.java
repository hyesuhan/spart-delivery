package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.OptionGroupSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OptionGroupSpecRepository extends JpaRepository<OptionGroupSpec, UUID> {
}