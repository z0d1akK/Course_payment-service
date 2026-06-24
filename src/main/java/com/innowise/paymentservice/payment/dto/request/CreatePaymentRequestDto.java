package com.innowise.paymentservice.payment.dto.request;

import com.innowise.paymentservice.common.constants.messages.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDto {

    @Schema(description = "Order identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = ValidationMessages.PAYMENT_ORDER_ID_REQUIRED)
    private UUID orderId;

    @Schema(description = "Payment amount", example = "199.99")
    @NotNull(message = ValidationMessages.PAYMENT_AMOUNT_REQUIRED)
    @DecimalMin(value = "0.01", message = ValidationMessages.PAYMENT_AMOUNT_POSITIVE)
    @Digits(integer = 10, fraction = 2, message = ValidationMessages.PAYMENT_AMOUNT_MAX_FRACTION)
    private BigDecimal paymentAmount;
}
