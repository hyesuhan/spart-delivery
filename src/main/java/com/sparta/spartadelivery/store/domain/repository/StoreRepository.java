package com.sparta.spartadelivery.store.domain.repository;

import com.sparta.spartadelivery.store.domain.entity.Store;

import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    @Query("""
            select s
            from Store s
            where s.deletedAt is null
              and s.isHidden = false
            """)
    Page<Store> findAllPublicStores(Pageable pageable);

    Page<Store> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Store> findByOwner(UserEntity user);
}
