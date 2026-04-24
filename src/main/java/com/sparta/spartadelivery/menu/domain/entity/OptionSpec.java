package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_option_spec")
public class OptionSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_spec_id")
    private UUID id;

    // 옵션 그룹 스펙 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_spec_id", nullable = false)
    private OptionGroupSpec optionGroupSpec;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Boolean isDefault = false;
}