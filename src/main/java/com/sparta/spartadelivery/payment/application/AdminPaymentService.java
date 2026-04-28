package com.sparta.spartadelivery.payment.application;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.payment.domain.entity.Payment;
import com.sparta.spartadelivery.payment.domain.entity.PaymentStatus;
import com.sparta.spartadelivery.payment.domain.repository.PaymentRepository;
import com.sparta.spartadelivery.payment.domain.validator.AdminPaymentValidator;
import com.sparta.spartadelivery.payment.exception.PayErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminPaymentService {

    private AdminPaymentValidator validator;
    private PaymentRepository paymentRepository;

    @Transactional
    public void updatePaymentStatus(Long userId, UUID paymentId, PaymentStatus paymentStatus) {
        // by manager or master
        validator.isAdmin(userId);

        Payment payment = getPaymentById(paymentId);

        payment.updateStatus(paymentStatus);

    }

    @Transactional
    public void deletePayment(Long userId, UUID paymentId) {
        // only master
        String masterName = validator.isMaster(userId);

        Payment payment = getPaymentById(paymentId);

        payment.delete(masterName);
        payment.updateStatus(PaymentStatus.CANCELLED);
    }

    private Payment getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(PayErrorCode.PAYMENT_NOT_FOUND));
    }
}
