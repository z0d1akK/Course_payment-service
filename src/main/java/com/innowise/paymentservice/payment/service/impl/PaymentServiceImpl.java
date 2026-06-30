package com.innowise.paymentservice.payment.service.impl;

import com.innowise.paymentservice.common.constants.messages.ErrorMessages;
import com.innowise.paymentservice.payment.dto.request.CreatePaymentRequestDto;
import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.dto.response.PaymentSummaryResponseDto;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.exception.PaymentNotFoundException;
import com.innowise.paymentservice.payment.mapper.PaymentMapper;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import com.innowise.paymentservice.payment.service.PaymentProcessingService;
import com.innowise.paymentservice.payment.service.PaymentService;
import com.innowise.paymentservice.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentProcessingService paymentProcessingService;

    @Override
    public PaymentResponseDto createPayment(UUID userId, CreatePaymentRequestDto requestDto) {
        Payment payment = paymentMapper.toEntity(requestDto);

        payment.setId(UUID.randomUUID());
        payment.setUserId(userId);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(Instant.now());

        Payment savedPayment = paymentRepository.save(payment);

        paymentProcessingService.processPayment(savedPayment.getId());

        return paymentMapper.toResponseDto(savedPayment);
    }

    @Override
    public PaymentResponseDto getPaymentById(UUID paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (!SecurityUtils.isAdmin() && !payment.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException(ErrorMessages.ACCESS_DENIED);
        }

        return paymentMapper.toResponseDto(payment);
    }

    @Override
    public Page<PaymentResponseDto> getPayments(PaymentFilterRequestDto filterDto, Pageable pageable) {

        UUID userId = filterDto.getUserId();

        if (!SecurityUtils.isAdmin()) {
            userId = SecurityUtils.getCurrentUserId();
        }

        Page<Payment> payments = paymentRepository.findPayments(
                userId,
                filterDto.getOrderId(),
                filterDto.getStatus(),
                pageable
        );

        return payments.map(paymentMapper::toResponseDto);
    }

    @Override
    public PaymentSummaryResponseDto getUserPaymentsSummary(UUID userId, Instant from, Instant to) {

        BigDecimal total = paymentRepository.calculateUserPaymentsSum(userId, from, to);

        return PaymentSummaryResponseDto.builder()
                .from(from)
                .to(to)
                .totalAmount(total != null ? total : BigDecimal.ZERO)
                .build();
    }

    @Override
    public PaymentSummaryResponseDto getAllPaymentsSummary(Instant from, Instant to) {

        BigDecimal total = paymentRepository.calculateAllPaymentsSum(from, to);

        return PaymentSummaryResponseDto.builder()
                .from(from)
                .to(to)
                .totalAmount(total != null ? total : BigDecimal.ZERO)
                .build();
    }
}
