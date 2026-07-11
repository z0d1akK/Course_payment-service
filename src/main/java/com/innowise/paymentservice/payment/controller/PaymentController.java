package com.innowise.paymentservice.payment.controller;

import com.innowise.paymentservice.common.dto.response.ApiErrorResponse;
import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.dto.response.PaymentSummaryResponseDto;
import com.innowise.paymentservice.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get payment by id", description = "Returns payment details")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "401", description = "Unauthorized user detected",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Payment not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get payments", description = "Returns payments using filters")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Payments retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized user detected",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping
    public ResponseEntity<Page<PaymentResponseDto>> getPayments(@ParameterObject PaymentFilterRequestDto filter, Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPayments(filter, pageable));
    }

    @Operation(summary = "Get user payments summary", description = "Returns total amount of successful payments for user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Summary retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized user detected",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PreAuthorize("@ownershipService.isOwnerOrAdmin(#userId)")
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<PaymentSummaryResponseDto> getUserPaymentsSummary(@PathVariable UUID userId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to
    ) {
        return ResponseEntity.ok(paymentService.getUserPaymentsSummary(userId, from, to));
    }

    @Operation(summary = "Get all payments summary", description = "Returns total amount of successful payments across platform")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Summary retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized user detected",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(responseCode = "403", description = "Access denied",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/summary")
    public ResponseEntity<PaymentSummaryResponseDto> getAllPaymentsSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to
    ) {
        return ResponseEntity.ok(paymentService.getAllPaymentsSummary(from, to));
    }
}