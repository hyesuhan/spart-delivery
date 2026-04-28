package com.sparta.spartadelivery.store.domain.repository;

import com.sparta.spartadelivery.store.domain.entity.Store;

import java.util.Optional;
import java.util.UUID;

import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByOwner(UserEntity user);

}
