package com.sparta.spartadelivery.address.domain.repository;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> finAllByUserDeletedAtIsNull(UserEntity user);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Address a SET a.isDefault = false " +
            "WHERE a.user.username = :username AND a.isDefault = true")
    void updateAllDefaultToFalse(@Param("username") String username);
}
