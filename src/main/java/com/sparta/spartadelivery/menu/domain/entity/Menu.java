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

<<<<<<< HEAD
    /*
    // 메뉴 여러 개는 한 가게에 속할 수 있습니다. (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
     */
=======
    // 메뉴 판매 가게 (N : 1)
    @Column(nullable = false)
    private UUID storeId;

    // 메뉴 카테고리 (N : 1)
    @Column(nullable = false)
    private UUID menuCategoryId;
>>>>>>> 395fafc (feat: #11 Menu 엔티티 및 Repository 추가)

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;
<<<<<<< HEAD
}
=======

    @Column(columnDefinition = "TEXT")
    private String aiDescription;

    @Column(columnDefinition = "TEXT")
    private String aiPrompt;

    public Menu(UUID storeId,
                UUID menuCategoryId,
                String name,
                Integer price,
                String description,
                String menuPictureUrl,
                Boolean isHidden,
                String aiDescription,
                String aiPrompt) {

        this.storeId = storeId;
        this.menuCategoryId = menuCategoryId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.menuPictureUrl = menuPictureUrl;
        this.isHidden = isHidden;
        this.aiDescription = aiDescription;
        this.aiPrompt = aiPrompt;
    }
}
>>>>>>> 395fafc (feat: #11 Menu 엔티티 및 Repository 추가)
