package com.sparta.spartadelivery.address.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_address")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(length = 50)
    private String alias;

    @Column(nullable = false)
    private String address;

    private String detail;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    private boolean isDefault = false;
}
