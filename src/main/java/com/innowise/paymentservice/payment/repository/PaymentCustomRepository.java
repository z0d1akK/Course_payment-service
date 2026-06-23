package com.innowise.paymentservice.payment.repository;

import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentCustomRepository {

    List<Payment> findPayments(UUID userId, UUID orderId, PaymentStatus status);

    BigDecimal calculateUserPaymentsSum(UUID userId, Instant from, Instant to);

    BigDecimal calculateAllPaymentsSum(Instant from, Instant to);
}