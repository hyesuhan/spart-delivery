package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.order.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    // 주문자 (N : 1)
    @Column(name = "customer_id", nullable = false)
    private Long customerId;


    // 현재 이로 대체합니다.
    @Column(name = "store_id", nullable = false)
    private UUID storeId;


    // 배송지 (N : 1)
    @Column(name = "address_id", nullable = false)
    private UUID addressId;


    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderType orderType = OrderType.ONLINE; // 기본값은 ONLINE으로 설정

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status; // 기본값은 PENDING으로 설정

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(columnDefinition = "TEXT")
    private String request;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Long customerId, UUID storeId, UUID addressId, List<OrderItem> items, String request) {
        validateItems(items);
        int totalPrice = items.stream().mapToInt(OrderItem::getSubTotal).sum();
        Order order = Order.builder()
                .customerId(customerId)
                .storeId(storeId)
                .addressId(addressId)
                .totalPrice(totalPrice)
                .request(request)
                .build();
        order.orderItems.addAll(items);
        return order;
    }

    public void cancel(LocalDateTime now) {
        if (now.isAfter(this.getCreatedAt().plusMinutes(5))) {
            throw new AppException(OrderErrorCode.ORDER_CANCEL_TIMEOUT);
        }
        this.status = OrderStatus.CANCELED;
    }


    //** private method **//
    @Builder
    private Order(Long customerId, UUID storeId, UUID addressId, Integer totalPrice, String request) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.totalPrice = totalPrice;
        this.request = request;
        this.status = OrderStatus.PENDING;
    }

    //** validator **//
    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new AppException(OrderErrorCode.ORDER_ITEMS_EMPTY);
        }
    }
}
