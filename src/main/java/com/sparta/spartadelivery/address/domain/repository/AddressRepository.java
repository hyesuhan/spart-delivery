package com.sparta.spartadelivery.address.domain.repository;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> finAllByUserDeletedAtIsNull(UserEntity user);
}
