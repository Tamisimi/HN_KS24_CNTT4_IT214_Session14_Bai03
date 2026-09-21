package com.storex.saga.shipping;

import com.storex.saga.event.SagaEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ShippingSagaService {

    private static final Logger log = LoggerFactory.getLogger(ShippingSagaService.class);

    private final ApplicationEventPublisher publisher;

    public ShippingSagaService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    public void onPaymentSuccess(SagaEvents.PaymentSuccess event) {
        // Mô phỏng: địa chỉ không hỗ trợ → fail (đổi logic nếu test happy path)
        boolean addressSupported = true; // đổi false để test bù trừ

        if (!addressSupported) {
            log.warn("[Shipping] FAILED orderId={}", event.getOrderId());
            publisher.publishEvent(new SagaEvents.ShippingFailed(event.getOrderId(), "Address not supported"));
            return;
        }

        String tracking = "VN" + event.getOrderId();
        log.info("[Shipping] SUCCESS orderId={} tracking={}", event.getOrderId(), tracking);
        publisher.publishEvent(new SagaEvents.ShippingSuccess(event.getOrderId(), tracking));
    }
}
