package com.sparta.spartadelivery.order.application;


import com.sparta.spartadelivery.payment.presentation.dto.request.PaymentCompletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            log.info("event received: updating order {} status to PAID", event.orderId());

            orderService.markOrderAsPaid(event.orderId());
        } catch (Exception e) {
            // failover logic needed
            log.error("Failed to update order status for oder id: {}", event.orderId());
        }
    }
}
