package com.sparta.spartadelivery.payment.domain.repository;

import com.sparta.spartadelivery.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
