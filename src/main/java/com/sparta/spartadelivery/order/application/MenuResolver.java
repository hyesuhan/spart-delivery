package com.sparta.spartadelivery.order.application;

import java.util.List;
import java.util.UUID;

public interface MenuResolver {
    // menu 이후 대체될 예정입니다.
    List<MenuSnapshot> resolveMenus(List<UUID> menuIds);
}
