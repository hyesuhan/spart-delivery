package com.sparta.spartadelivery.review.domain.repository;

import com.sparta.spartadelivery.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
