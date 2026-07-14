package com.innowise.paymentservice.payment.dto.response;

import com.innowise.paymentservice.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    @Schema(description = "Payment identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Order identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

    @Schema(description = "User identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Payment status", example = "SUCCESS")
    private PaymentStatus status;

    @Schema(description = "Payment amount", example = "199.99")
    private BigDecimal paymentAmount;

    @Schema(description = "Payment creation timestamp", example = "2026-06-23T12:15:30Z")
    private Instant timestamp;
}