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
@Table(name = "p_menu")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id")
    private UUID id;

    /*
    // 메뉴 여러 개는 한 가게에 속할 수 있습니다. (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
     */

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
}
