package com.storex.saga.order;

import com.storex.saga.event.SagaEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Order Service — tạo đơn + phản ứng event (choreography).
 */
@Service
public class OrderSagaService {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaService.class);

    private final ApplicationEventPublisher publisher;
    /** orderId → amount (để bù trừ) */
    private final Map<String, Long> pendingPayments = new ConcurrentHashMap<>();
    /** orderId → deadline chờ Shipping */
    private final Map<String, Instant> shippingDeadlines = new ConcurrentHashMap<>();
    private final Map<String, String> orderStatus = new ConcurrentHashMap<>();

    public OrderSagaService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public String createOrder(String orderId, String customerId, long amount, String address) {
        orderStatus.put(orderId, "PENDING");
        log.info("[Order] PENDING orderId={}", orderId);
        publisher.publishEvent(new SagaEvents.OrderCreated(orderId, customerId, amount, address));
        return orderId;
    }

    @EventListener
    public void onPaymentSuccess(SagaEvents.PaymentSuccess event) {
        pendingPayments.put(event.getOrderId(), event.getAmount());
        shippingDeadlines.put(event.getOrderId(), Instant.now().plusSeconds(30));
        log.info("[Order] Đã thanh toán, chờ Shipping (timeout 30s) orderId={}", event.getOrderId());
    }

    @EventListener
    public void onPaymentFailed(SagaEvents.PaymentFailed event) {
        orderStatus.put(event.getOrderId(), "CANCELED");
        log.info("[Order] CANCELED do PaymentFailed orderId={}", event.getOrderId());
    }

    @EventListener
    public void onShippingSuccess(SagaEvents.ShippingSuccess event) {
        shippingDeadlines.remove(event.getOrderId());
        pendingPayments.remove(event.getOrderId());
        orderStatus.put(event.getOrderId(), "COMPLETED");
        log.info("[Order] COMPLETED orderId={} tracking={}", event.getOrderId(), event.getTrackingCode());
    }

    @EventListener
    public void onShippingFailed(SagaEvents.ShippingFailed event) {
        triggerCompensation(event.getOrderId(), event.getReason());
    }

    @EventListener
    public void onRefundSuccess(SagaEvents.RefundSuccess event) {
        orderStatus.put(event.getOrderId(), "CANCELED");
        log.info("[Order] CANCELED sau RefundSuccess orderId={}", event.getOrderId());
    }

    /** Gọi từ timeout job */
    public void triggerCompensation(String orderId, String reason) {
        if (!"PENDING".equals(orderStatus.get(orderId))
                && orderStatus.get(orderId) != null
                && !"PENDING".equals(orderStatus.get(orderId))) {
            // nếu đã COMPLETED/CANCELED thì bỏ qua
        }
        Long amount = pendingPayments.remove(orderId);
        shippingDeadlines.remove(orderId);
        if (amount == null) {
            orderStatus.put(orderId, "CANCELED");
            return;
        }
        log.warn("[Order] Bù trừ payment orderId={} reason={}", orderId, reason);
        publisher.publishEvent(new SagaEvents.CompensatePayment(orderId, amount));
    }

    public Map<String, Instant> getShippingDeadlines() {
        return shippingDeadlines;
    }

    public String status(String orderId) {
        return orderStatus.getOrDefault(orderId, "UNKNOWN");
    }
}
