package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_order")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID id;

    /*
    // 주문자 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer;
     */

    /*
    // 주문 가게 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
     */

    /*
    // 배송지 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
     */

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderType type = OrderType.ONLINE; // 기본값은 ONLINE으로 설정

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING; // 기본값은 PENDING으로 설정

    private Integer totalPrice;

    @Column(columnDefinition = "TEXT")
    private String request;

    /*
    // 이는 추후 수정될 예정입니다. (이유: setter)
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }

     */



}
