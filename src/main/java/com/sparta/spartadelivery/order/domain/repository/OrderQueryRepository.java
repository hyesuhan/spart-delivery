package com.sparta.spartadelivery.order.domain.repository;

import com.sparta.spartadelivery.order.domain.entity.Order;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface OrderQueryRepository {

    // 동적 쿼리용 레포 - 추후 query dsl 추가 예정
    // Page<Order> findAllByStore(OrderSearchStore store, Pageable pageable);
}
