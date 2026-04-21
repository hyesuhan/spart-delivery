package com.sparta.spartadelivery.store.domain.entity;

import com.sparta.spartadelivery.area.domain.entity.Area;
import com.sparta.spartadelivery.category.domain.entity.Category;
import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "average_rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @Builder
    private Store(
            UserEntity owner,
            Category category,
            Area area,
            String name,
            String address,
            String phone
    ) {
        this.owner = owner;
        this.category = category;
        this.area = area;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void update(Category category, Area area, String name, String address, String phone) {
        this.category = category;
        this.area = area;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void updateAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }
}
