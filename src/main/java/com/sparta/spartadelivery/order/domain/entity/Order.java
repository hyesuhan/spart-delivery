package com.sparta.spartadelivery.order.domain.entity;

import com.sparta.spartadelivery.address.domain.entity.Address;
import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private UserEntity customer;


    /*
    // 주문 가게 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
     */


    // 배송지 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;


    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderType type = OrderType.ONLINE; // 기본값은 ONLINE으로 설정

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING; // 기본값은 PENDING으로 설정

    private Integer totalPrice;

    @Column(columnDefinition = "TEXT")
    private String request;

    @Builder
    public Order(UserEntity user, Address address, Integer totalPrice, String request) {
        this.customer = user;
        this.address = address;
        validTotalPrice(totalPrice);
        this.totalPrice = totalPrice;
        this.request = request;
    }

    public void cancel() {
        validCancelTime(this.getCreatedAt());
        this.status = OrderStatus.CANCELED;

    }

    public void updateRequest(String request) {
        validUpdateRequest(this.status);
        this.request = request;
    }

    private void  validTotalPrice(Integer totalPrice) {
        if (totalPrice < 0) {
            throw new IllegalArgumentException("주문 총액은 음수일 수 없습니다.");
        }
    }

    private void validCancelTime(LocalDateTime createdAt) {
        if (LocalDateTime.now().isAfter(createdAt.plusMinutes(5))) {
            throw new IllegalStateException("주문은 생성 후 5분 이내에만 취소할 수 있습니다.");
        }
    }

    private void validUpdateRequest(OrderStatus status) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("요청사항은 주문이 PENDING 상태일 때만 수정할 수 있습니다.");
        }
    }


}
