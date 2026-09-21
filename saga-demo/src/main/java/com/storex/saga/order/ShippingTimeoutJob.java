package com.storex.saga.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Timeout 30s chờ Shipping — kích hoạt bù trừ như ShippingFailed.
 */
@Component
public class ShippingTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(ShippingTimeoutJob.class);

    private final OrderSagaService orderSagaService;

    public ShippingTimeoutJob(OrderSagaService orderSagaService) {
        this.orderSagaService = orderSagaService;
    }

    @Scheduled(fixedDelay = 5000)
    public void checkTimeouts() {
        Instant now = Instant.now();
        List<String> timedOut = new ArrayList<>();

        for (Map.Entry<String, Instant> e : orderSagaService.getShippingDeadlines().entrySet()) {
            if (now.isAfter(e.getValue())) {
                timedOut.add(e.getKey());
            }
        }

        for (String orderId : timedOut) {
            log.warn("[Order] Shipping timeout 30s orderId={}", orderId);
            orderSagaService.triggerCompensation(orderId, "Shipping timeout 30s");
        }
    }
}
