package com.innowise.paymentservice.payment.service;

import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.dto.response.PaymentSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface PaymentService {

    /**
     * Creates a new payment after receiving CREATE_ORDER event.
     *
     * @param orderId order identifier
     * @param userId payment owner
     * @param amount payment amount
     */
    void createPayment(UUID orderId, UUID userId, BigDecimal amount);

    /**
     * Returns payment details by identifier.
     *
     * @param paymentId payment identifier
     * @return payment details response
     */
    PaymentResponseDto getPaymentById(UUID paymentId);

    /**
     * Returns payments matching provided filter criteria.
     * <p>
     * Supports filtering by user identifier,
     * order identifier and payment status.
     *
     * @param filterDto filtering parameters
     * @return list of matching payments
     */
    Page<PaymentResponseDto> getPayments(PaymentFilterRequestDto filterDto, Pageable pageable);

    /**
     * Calculates the total amount of successful payments
     * for a specific user within the given date range.
     * <p>
     * Uses MongoDB aggregation pipeline.
     *
     * @param userId user identifier
     * @param from start date and time
     * @param to end date and time
     * @return payment summary response
     */
    PaymentSummaryResponseDto getUserPaymentsSummary(UUID userId, Instant from, Instant to);

    /**
     * Calculates the total amount of successful payments
     * across the entire platform within the given date range.
     * <p>
     * Uses MongoDB aggregation pipeline.
     *
     * @param from start date and time
     * @param to end date and time
     * @return payment summary response
     */
    PaymentSummaryResponseDto getAllPaymentsSummary(Instant from, Instant to);
}