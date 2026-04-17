package com.sparta.spartadelivery.orderItem.domain.entity;

import com.sparta.spartadelivery.order.domain.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table(name = "p_order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;
     */

    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice; // 주문 당시 단기 (스냅샷)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false, length = 100)
    private String createdBy;

    /*
    // 내부 메서드 - 이는 수정 가능성이 있습니다.
    protected void setOrder(Order order) {
        this.order = order;
     */
}
