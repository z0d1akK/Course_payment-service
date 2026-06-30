package com.innowise.paymentservice.payment.service;

import java.util.UUID;

public interface PaymentProcessingService {

    /**
     * Processes payment asynchronously.
     * <p>
     * Calls external payment gateway simulation,
     * determines payment result, updates payment status
     * and publishes PAYMENT_COMPLETED event to Kafka.
     *
     * @param paymentId payment identifier
     */
    void processPayment(UUID paymentId);
}