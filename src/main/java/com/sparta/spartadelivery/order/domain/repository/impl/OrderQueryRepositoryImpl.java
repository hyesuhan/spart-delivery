package com.sparta.spartadelivery.order.domain.repository.impl;

import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.order.domain.repository.OrderQueryRepository;
import com.sparta.spartadelivery.order.presentation.dto.request.SearchOrdersVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QOrder order = QOrder.order;

    @Override
    public Page<Order> searchOrders(SearchOrdersVO searchOrdersVO, Pageable pageable) {
        return null;
    }
}
