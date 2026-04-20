package com.sparta.spartadelivery.area.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_area")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "area_id")
    private UUID id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "city", length = 50, nullable = false)
    private String city;

    @Column(name = "district", length = 50, nullable = false)
    private String district;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder
    private Area(String name, String city, String district, Boolean active) {
        this.name = name;
        this.city = city;
        this.district = district;
        this.active = active == null || active;
    }

    public void update(String name, String city, String district, boolean active) {
        this.name = name;
        this.city = city;
        this.district = district;
        this.active = active;
    }
}
