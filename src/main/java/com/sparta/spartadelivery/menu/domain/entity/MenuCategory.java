package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_category_id")
    private UUID id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    /*

    // 상위 카테고리
    @Column(nullable = True)
    private UUID parent_id;


    // 해당 메뉴 카테고리를 가진 가게 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

     */

    /*

    @Builder
    private MenuCategory(String name) {
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }

     */
}