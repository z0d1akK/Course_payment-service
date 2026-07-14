package com.innowise.paymentservice.payment.service.impl;

import com.innowise.paymentservice.client.random.service.RandomApiService;
import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import com.innowise.paymentservice.kafka.producer.PaymentEventProducer;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.exception.PaymentNotFoundException;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import com.innowise.paymentservice.payment.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PaymentRepository paymentRepository;

    private final RandomApiService randomApiService;

    private final PaymentEventProducer paymentEventProducer;

    @Async
    @Override
    public void processPayment(UUID paymentId) {

        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));

            boolean success = randomApiService.isEvenNumber();

            payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

            paymentRepository.save(payment);

            paymentEventProducer.publishPaymentCompletedEvent(
                    PaymentCompletedEvent.builder()
                            .orderId(payment.getOrderId())
                            .status(payment.getStatus())
                            .build()
            );

        } catch (Exception exception) {

            log.error(
                    "Payment processing failed. paymentId={}",
                    paymentId,
                    exception
            );

            paymentRepository.findById(paymentId).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
            });
        }
    }
}