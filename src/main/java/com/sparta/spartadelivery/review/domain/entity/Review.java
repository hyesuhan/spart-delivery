package com.sparta.spartadelivery.review.domain.entity;

import com.sparta.spartadelivery.global.entity.BaseEntity;
import com.sparta.spartadelivery.order.domain.entity.Order;
import com.sparta.spartadelivery.store.domain.entity.Store;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
public class Review extends BaseEntity {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity customer;

    private int rating;

    private String content;

    protected Review() {
    }

    public Review(UUID id, Order order, Store store, UserEntity customer, int rating, String content) {
        this.id = id;
        this.order = order;
        this.store = store;
        this.customer = customer;
        this.rating = rating;
        this.content = content;
        validate();
    }

    public Review(Order order, Store store, UserEntity customer, int rating, String content) {
        this(UUID.randomUUID(), order, store, customer, rating, content);
    }

    private void validate() {
//        if (!Objects.equals(order.getCustomer().getId(), customer.getId())) {
//            throw new IllegalStateException("주문자와 리뷰 작성자가 일치하지 않으면 리뷰 생성할 수 없습니다");
//        }

//        if (isOutOfRange(rating)) {
//            throw new IllegalArgumentException(String.format("리뷰 점수는 %d부터 %d까지 입력할 수 있습니다.", MIN_RATING, MAX_RATING));
//        }

//        if (order.getStatus() != OrderStatus.COMPLETED) {
//            throw new IllegalStateException("주문이 완료되지 않은 경우 리뷰를 생성할 수 없습니다");
//        }
    }

    public void update(Long loginUserId, int rating, String content) {
        verifyCustomer(loginUserId);
        validateRating(rating);
        this.rating = rating;
        this.content = content;
    }

    public void delete(Long loginUserId, String userName) {
        verifyCustomer(loginUserId);
        super.markDeleted(userName);
    }

    private void verifyCustomer(Long loginUserId) {
        if (!loginUserId.equals(this.customer.getId())) {
            throw new IllegalArgumentException("리뷰 작성자만 수정 가능합니다");
        }
    }

    private void validateRating(int rating) {
        if (isOutOfRange(rating))
            throw new IllegalArgumentException(String.format("리뷰 점수는 %d부터 %d까지 입력할 수 있습니다.", MIN_RATING, MAX_RATING));
    }

    private boolean isOutOfRange(int rating) {
        return rating < MIN_RATING || rating > MAX_RATING;
    }
}
