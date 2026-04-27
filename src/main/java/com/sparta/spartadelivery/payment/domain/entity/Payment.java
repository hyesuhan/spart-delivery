package com.sparta.spartadelivery.payment.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.order.domain.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 이벤트 기반으로 처리 합니다.
    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CARD; // 현재는 CARD만 가능

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer amount;

    public static Payment create(UUID orderId, Integer amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

}
