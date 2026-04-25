package com.sparta.spartadelivery.menu.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_tag_id")
    private UUID id;

    // 메뉴 (N : 1)
    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    // 태그 (N : 1)
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    public MenuTag(UUID menuId, UUID tagId) {
        this.menuId = menuId;
        this.tagId = tagId;
    }
}
