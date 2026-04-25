package com.sparta.spartadelivery.menu.domain.repository;

import com.sparta.spartadelivery.menu.domain.entity.OptionSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OptionSpecRepository extends JpaRepository<OptionSpec, UUID> {
}
