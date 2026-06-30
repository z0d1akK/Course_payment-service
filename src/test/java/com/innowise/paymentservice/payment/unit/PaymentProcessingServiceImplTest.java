package com.innowise.paymentservice.payment.unit;

import com.innowise.paymentservice.client.random.service.RandomApiService;
import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import com.innowise.paymentservice.kafka.producer.PaymentEventProducer;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import com.innowise.paymentservice.payment.service.impl.PaymentProcessingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.innowise.paymentservice.payment.testclasses.PaymentTestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RandomApiService randomApiService;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentProcessingServiceImpl service;

    @Test
    @DisplayName("Should mark payment as SUCCESS and publish Kafka event")
    void processPayment_WhenSuccessful() {
        Payment payment = createPayment();

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(randomApiService.isEvenNumber()).thenReturn(true);

        service.processPayment(payment.getId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(paymentRepository).save(payment);

        ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);

        verify(paymentEventProducer).publishPaymentCompletedEvent(captor.capture());

        assertThat(captor.getValue().getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should mark payment as FAILED and publish Kafka event")
    void processPayment_WhenFailed() {
        Payment payment = createPayment();

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        when(randomApiService.isEvenNumber()).thenReturn(false);

        service.processPayment(payment.getId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentRepository).save(payment);
        verify(paymentEventProducer).publishPaymentCompletedEvent(any());
    }

    @Test
    @DisplayName("Should mark payment FAILED when Random API throws exception")
    void processPayment_WhenRandomApiFails() {
        Payment payment = createPayment();

        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(randomApiService.isEvenNumber()).thenThrow(new RuntimeException());

        service.processPayment(payment.getId());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentRepository, times(2)).findById(payment.getId());
        verify(paymentRepository).save(payment);
        verify(paymentEventProducer, never()).publishPaymentCompletedEvent(any());
    }
}