package com.innowise.paymentservice.payment.service;

import com.innowise.paymentservice.payment.dto.request.CreatePaymentRequestDto;
import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.dto.response.PaymentSummaryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface PaymentService {

    /**
     * Creates a new payment for the specified user.
     * <p>
     * Payment is initially created with PENDING status and then
     * processed asynchronously by the payment gateway integration.
     *
     * @param userId identifier of the payment owner
     * @param requestDto request object containing payment information
     * @return created payment response
     */
    PaymentResponseDto createPayment(UUID userId, CreatePaymentRequestDto requestDto);

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