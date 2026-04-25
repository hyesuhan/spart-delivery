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
@Table(name = "p_option_group_spec")
public class OptionGroupSpec extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "option_group_spec_id")
    private UUID id;

    // 메뉴 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(length = 100, nullable = false)
    private String name;

    @Column // Default 0
    private Integer minSelect;

    @Column // Default 1
    private Integer maxSelect;

    public OptionGroupSpec(Menu menu, String name, Integer minSelect, Integer maxSelect) {
        this.menu = menu;
        this.name = name;
        this.minSelect = (minSelect != null) ? minSelect : 0;
        this.maxSelect = (maxSelect != null) ? maxSelect : 1;
    }

    // 생성 팩토리
    public static OptionGroupSpec createOptionGroupSpec(Menu menu, String name, Integer minSelect, Integer maxSelect) {
        return  new OptionGroupSpec(menu, name, minSelect, maxSelect);
    }
}