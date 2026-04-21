package com.sparta.spartadelivery.store.domain.repository;

import com.sparta.spartadelivery.store.domain.entity.Store;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
