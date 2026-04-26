package com.sparta.spartadelivery.order.domain.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.spartadelivery.order.domain.entity.OrderStatus;
import com.sparta.spartadelivery.order.domain.entity.QOrderItem;
import com.sparta.spartadelivery.order.presentation.dto.request.OrderSearchRequest;
import com.sparta.spartadelivery.order.presentation.dto.response.OrderSearch.OrderSearchResponse;
import com.sparta.spartadelivery.store.domain.entity.QStore;
import com.sparta.spartadelivery.user.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.sparta.spartadelivery.order.domain.entity.QOrder.order;
import static com.sparta.spartadelivery.order.domain.entity.QOrderItem.orderItem;
import static com.sparta.spartadelivery.store.domain.entity.QStore.store;


@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<OrderSearchResponse> searchOrders(OrderSearchRequest.SearchCondition cond, Pageable pageable) {
        // 첫 번째 상품명만 가져오는 서브쿼리
        QStore store = QStore.store;
        QOrderItem subItem = new QOrderItem("subItem");


        /*
        Expression<String> firstItemNameExpression = ExpressionUtils.as(
                JPAExpressions
                        .select(subItem.menuName) // OrderItem에 name 필드가 있다고 가정
                        .from(order.orderItems, subItem)
                        .limit(1),
                "firstItemName"
        );

         */

        List<OrderSearchResponse> content = jpaQueryFactory
                .select(Projections.constructor(OrderSearchResponse.class,
                        order.id,
                        order.customerId,
                        order.storeId,
                        store.name,
                        order.status,
                        orderItem.menuName.min(),
                        order.request,
                        order.createdAt
                ))
                .from(order)
                .leftJoin(store).on(order.storeId.eq(store.id))
                .leftJoin(order.orderItems, orderItem)
                .where(
                        userIdEq(cond.userId(), cond.userRole()),
                        storeIdEq(cond.ownedStoreId(), cond.storeId()),
                        statusEq(cond.orderStatus())
                )
                .groupBy(order.id, store.name)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(order.count()).from(order)
                .where(userIdEq(cond.userId(), cond.userRole()), storeIdEq(cond.ownedStoreId(), cond.storeId()), statusEq(cond.orderStatus()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression userIdEq(Long userId, Role role) {
        if (role == Role.CUSTOMER) return order.customerId.eq(userId);
        return null;
    }

    private BooleanExpression storeIdEq(UUID ownedStoreId, UUID searchStoreId) {
        if (ownedStoreId != null) return order.storeId.eq(ownedStoreId);
        return searchStoreId != null ? order.storeId.eq(searchStoreId) : null;
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    };
}
