package com.storex.saga.payment;

import com.storex.saga.event.SagaEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentSagaService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaService.class);

    private final ApplicationEventPublisher publisher;

    public PaymentSagaService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    public void onOrderCreated(SagaEvents.OrderCreated event) {
        log.info("[Payment] Trừ tiền orderId={} amount={}", event.getOrderId(), event.getAmount());
        // Giả sử luôn thành công; có thể nhánh fail theo nghiệp vụ
        publisher.publishEvent(new SagaEvents.PaymentSuccess(event.getOrderId(), event.getAmount()));
    }

    @EventListener
    public void onCompensatePayment(SagaEvents.CompensatePayment event) {
        log.info("[Payment] HOÀN TIỀN orderId={} amount={}", event.getOrderId(), event.getAmount());
        publisher.publishEvent(new SagaEvents.RefundSuccess(event.getOrderId()));
    }
}
