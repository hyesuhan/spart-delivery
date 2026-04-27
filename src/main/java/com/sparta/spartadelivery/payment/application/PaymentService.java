package com.sparta.spartadelivery.payment.application;

import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.repository.PaymentRepository;
import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentCompletedEvent;
import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processPayment(UUID orderId, Integer amount) {
        Payment payment = Payment.create(orderId, amount);
        paymentRepository.save(payment);

        log.info("Payment saved for Order Id : {}", orderId);

        eventPublisher.publishEvent(new PaymentCompletedEvent(orderId, payment.getId(), payment.getAmount()));
    }
}


