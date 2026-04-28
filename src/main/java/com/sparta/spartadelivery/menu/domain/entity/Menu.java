package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.menu.domain.vo.MoneyVO;
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

    // 메뉴 판매 가게 (N : 1)
    @Column(nullable = false)
    private UUID storeId;

    // 메뉴 카테고리 (N : 1)
    @Column(nullable = false)
    private UUID menuCategoryId;

    @Column(length = 100, nullable = false)
    private String name;

    @Embedded
    //@AttributeOverride(name = "price", column = @Column(name = "price", nullable = false))
    private MoneyVO price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String menuPictureUrl;

    @Column // Default false
    private boolean isHidden;

    @Column(columnDefinition = "TEXT")
    private String aiDescription;

    @Column(columnDefinition = "TEXT")
    private String aiPrompt;

    public Menu(
            UUID storeId,
            UUID menuCategoryId,
            String name,
            Integer price,
            String description,
            String menuPictureUrl,
            boolean isHidden,
            String aiDescription,
            String aiPrompt) {

        this.storeId = storeId;
        this.menuCategoryId = menuCategoryId;
        this.name = name;
        this.price = new MoneyVO(price);
        this.description = description;
        this.menuPictureUrl = menuPictureUrl;
        this.isHidden = isHidden;
        this.aiDescription = aiDescription;
        this.aiPrompt = aiPrompt;
    }

    public void update(
            UUID storeId,
            UUID menuCategoryId,
            String name,
            Integer price,
            String description,
            String menuPictureUrl,
            boolean isHidden,
            String aiDescription,
            String aiPrompt) {

        this.storeId = storeId;
        this.menuCategoryId = menuCategoryId;
        this.name = name;
        this.price = new  MoneyVO(price);
        this.description = description;
        this.menuPictureUrl = menuPictureUrl;
        this.isHidden = isHidden;
        this.aiDescription = aiDescription;
        this.aiPrompt = aiPrompt;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }
}