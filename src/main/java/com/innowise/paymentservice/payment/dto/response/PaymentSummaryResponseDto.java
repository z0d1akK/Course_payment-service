package com.innowise.paymentservice.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummaryResponseDto {

    @Schema(description = "Period start", example = "2026-01-01T00:00:00Z")
    private Instant from;

    @Schema(description = "Period end", example = "2026-12-31T23:59:59Z")
    private Instant to;

    @Schema(description = "Total successful payments amount", example = "12500.75")
    private BigDecimal totalAmount;
}
