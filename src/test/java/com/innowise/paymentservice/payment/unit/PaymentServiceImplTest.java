package com.innowise.paymentservice.payment.unit;

import com.innowise.paymentservice.payment.dto.request.PaymentFilterRequestDto;
import com.innowise.paymentservice.payment.dto.response.PaymentResponseDto;
import com.innowise.paymentservice.payment.dto.response.PaymentSummaryResponseDto;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.exception.PaymentNotFoundException;
import com.innowise.paymentservice.payment.mapper.PaymentMapper;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import com.innowise.paymentservice.payment.service.PaymentProcessingService;
import com.innowise.paymentservice.payment.service.impl.PaymentServiceImpl;
import com.innowise.paymentservice.security.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.innowise.paymentservice.payment.testclasses.PaymentTestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Should create payment with PENDING status and trigger async processing")
    void createPayment_ShouldCreatePaymentAndTriggerProcessing() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.<Payment>getArgument(0));

        paymentService.createPayment(ORDER_ID, USER_ID, AMOUNT);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(capturedPayment.getUserId()).isEqualTo(USER_ID);
        assertThat(capturedPayment.getPaymentAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(capturedPayment.getId()).isNotNull();
        assertThat(capturedPayment.getTimestamp()).isNotNull();

        verify(paymentProcessingService).processPayment(capturedPayment.getId());
    }

    @Test
    @DisplayName("Should return payment when payment exists and user is owner")
    void getPaymentById_WhenPaymentExists_ShouldReturnPayment() {
        Payment payment = createPayment();
        PaymentResponseDto response = paymentResponse();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(payment.getUserId());
            securityUtils.when(SecurityUtils::isAdmin).thenReturn(false);

            when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
            when(paymentMapper.toResponseDto(payment)).thenReturn(response);

            PaymentResponseDto result = paymentService.getPaymentById(payment.getId());

            assertThat(result).isEqualTo(response);

            verify(paymentRepository).findById(payment.getId());
            verify(paymentMapper).toResponseDto(payment);
        }
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment does not exist")
    void getPaymentById_WhenPaymentNotExists_ShouldThrowException() {
        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(PAYMENT_ID))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository).findById(PAYMENT_ID);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    @DisplayName("Should use filter userId when current user is admin")
    void getPayments_WhenAdmin_ShouldUseFilterUserId() {
        PaymentFilterRequestDto filter = paymentFilter();

        Pageable pageable = Pageable.unpaged();

        Payment payment = createPayment();

        PaymentResponseDto response = paymentResponse();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {

            securityUtils.when(SecurityUtils::isAdmin).thenReturn(true);

            when(paymentRepository.findPayments(
                    filter.getUserId(),
                    filter.getOrderId(),
                    filter.getStatus(),
                    pageable
            )).thenReturn(new PageImpl<>(List.of(payment)));

            when(paymentMapper.toResponseDto(payment))
                    .thenReturn(response);

            Page<PaymentResponseDto> result = paymentService.getPayments(filter, pageable);

            assertThat(result.getContent()).containsExactly(response);

            verify(paymentRepository).findPayments(
                    filter.getUserId(),
                    filter.getOrderId(),
                    filter.getStatus(),
                    pageable
            );

            verify(paymentMapper).toResponseDto(payment);

            securityUtils.verify(SecurityUtils::isAdmin);
            securityUtils.verifyNoMoreInteractions();
        }
    }

    @Test
    @DisplayName("Should use current authenticated user when current user is not admin")
    void getPayments_WhenUser_ShouldIgnoreFilterUserId() {
        UUID currentUserId = UUID.randomUUID();

        PaymentFilterRequestDto filter = paymentFilter();

        Pageable pageable = Pageable.unpaged();

        Payment payment = createPayment();

        PaymentResponseDto response = paymentResponse();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {

            securityUtils.when(SecurityUtils::isAdmin).thenReturn(false);
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

            when(paymentRepository.findPayments(
                    currentUserId,
                    filter.getOrderId(),
                    filter.getStatus(),
                    pageable
            )).thenReturn(new PageImpl<>(List.of(payment)));

            when(paymentMapper.toResponseDto(payment)).thenReturn(response);

            Page<PaymentResponseDto> result = paymentService.getPayments(filter, pageable);

            assertThat(result.getContent()).containsExactly(response);

            verify(paymentRepository).findPayments(
                    currentUserId,
                    filter.getOrderId(),
                    filter.getStatus(),
                    pageable
            );

            verify(paymentMapper).toResponseDto(payment);

            securityUtils.verify(SecurityUtils::isAdmin);
            securityUtils.verify(SecurityUtils::getCurrentUserId);
            securityUtils.verifyNoMoreInteractions();
        }
    }

    @Test
    @DisplayName("Should return user payments summary")
    void getUserPaymentsSummary_ShouldReturnSummary() {
        Instant from = Instant.now();

        Instant to = from.plusSeconds(3600);

        when(paymentRepository.calculateUserPaymentsSum(USER_ID, from, to)).thenReturn(BigDecimal.TEN);

        PaymentSummaryResponseDto result = paymentService.getUserPaymentsSummary(USER_ID, from, to);

        assertThat(result.getFrom()).isEqualTo(from);
        assertThat(result.getTo()).isEqualTo(to);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.TEN);

        verify(paymentRepository).calculateUserPaymentsSum(USER_ID, from, to);
    }

    @Test
    @DisplayName("Should return zero when user payments summary is null")
    void getUserPaymentsSummary_WhenRepositoryReturnsNull_ShouldReturnZero() {
        Instant from = Instant.now();

        Instant to = from.plusSeconds(3600);

        when(paymentRepository.calculateUserPaymentsSum(USER_ID, from, to)).thenReturn(null);

        PaymentSummaryResponseDto result = paymentService.getUserPaymentsSummary(USER_ID, from, to);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(paymentRepository).calculateUserPaymentsSum(USER_ID, from, to);
    }

    @Test
    @DisplayName("Should return all payments summary")
    void getAllPaymentsSummary_ShouldReturnSummary() {
        Instant from = Instant.now();

        Instant to = from.plusSeconds(3600);

        when(paymentRepository.calculateAllPaymentsSum(from, to)).thenReturn(BigDecimal.valueOf(250));

        PaymentSummaryResponseDto result = paymentService.getAllPaymentsSummary(from, to);

        assertThat(result.getFrom()).isEqualTo(from);
        assertThat(result.getTo()).isEqualTo(to);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));

        verify(paymentRepository).calculateAllPaymentsSum(from, to);
    }

    @Test
    @DisplayName("Should return zero when all payments summary is null")
    void getAllPaymentsSummary_WhenRepositoryReturnsNull_ShouldReturnZero() {
        Instant from = Instant.now();

        Instant to = from.plusSeconds(3600);

        when(paymentRepository.calculateAllPaymentsSum(from, to)).thenReturn(null);

        PaymentSummaryResponseDto result = paymentService.getAllPaymentsSummary(from, to);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(paymentRepository).calculateAllPaymentsSum(from, to);
    }
}