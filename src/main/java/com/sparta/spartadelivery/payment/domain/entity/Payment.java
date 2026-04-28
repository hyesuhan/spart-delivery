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

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod; // 현재는 CARD만 가능

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer amount;

    public static Payment create(UUID orderId, PaymentMethod method, Integer amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.paymentMethod = method;
        payment.amount = amount;
        payment.status = PaymentStatus.COMPLETED;

        return payment;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

    public void delete(String deletedBy) {
        this.markDeleted(deletedBy);
    }

}
