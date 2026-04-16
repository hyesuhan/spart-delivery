package com.sparta.spartadelivery.user.domain.repository;

import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {


    Optional<Object> findByEmailAndDeletedAtIsNull(String email);
}
