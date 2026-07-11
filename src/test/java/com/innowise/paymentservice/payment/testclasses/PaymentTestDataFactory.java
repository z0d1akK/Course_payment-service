package com.innowise.paymentservice.payment.testclasses;

import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PaymentTestDataFactory {

    private PaymentTestDataFactory() {
    }

    public static final UUID PAYMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static final Instant TIMESTAMP = Instant.parse("2026-06-27T12:00:00Z");

    public static final BigDecimal AMOUNT = BigDecimal.valueOf(150.00);

    public static PaymentFilterRequestDto paymentFilter() {
        return PaymentFilterRequestDto.builder()
                .userId(USER_ID)
                .orderId(ORDER_ID)
                .status(PaymentStatus.SUCCESS)
                .build();
    }

    public static Payment createPayment() {
        return Payment.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(BigDecimal.valueOf(150))
                .status(PaymentStatus.PENDING)
                .timestamp(TIMESTAMP)
                .build();
    }

    public static Payment createSuccessfulPayment() {
        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(Instant.now());
        return payment;
    }

    public static PaymentResponseDto paymentResponse() {
        return PaymentResponseDto.builder()
                .id(PAYMENT_ID)
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(BigDecimal.valueOf(150))
                .status(PaymentStatus.PENDING)
                .timestamp(TIMESTAMP)
                .build();
    }
}