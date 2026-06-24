package com.innowise.paymentservice.payment.dto.request;

import com.innowise.paymentservice.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFilterRequestDto {

    @Schema(description = "User identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID userId;

    @Schema(description = "Order identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID orderId;

    @Schema(description = "Payment status", example = "SUCCESS")
    private PaymentStatus status;
}