package com.storex.saga.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

public final class SagaEvents {

    private SagaEvents() {}

    @Getter
    @AllArgsConstructor
    public static class OrderCreated {
        private final String orderId;
        private final String customerId;
        private final long amount;
        private final String address;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentSuccess {
        private final String orderId;
        private final long amount;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentFailed {
        private final String orderId;
        private final String reason;
    }

    @Getter
    @AllArgsConstructor
    public static class ShippingSuccess {
        private final String orderId;
        private final String trackingCode;
    }

    @Getter
    @AllArgsConstructor
    public static class ShippingFailed {
        private final String orderId;
        private final String reason;
    }

    @Getter
    @AllArgsConstructor
    public static class CompensatePayment {
        private final String orderId;
        private final long amount;
    }

    @Getter
    @AllArgsConstructor
    public static class RefundSuccess {
        private final String orderId;
    }
}
