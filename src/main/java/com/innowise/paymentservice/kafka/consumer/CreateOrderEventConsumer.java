package com.innowise.paymentservice.kafka.consumer;

import com.innowise.paymentservice.kafka.event.CreateOrderEvent;
import com.innowise.paymentservice.kafka.properties.KafkaTopics;
import com.innowise.paymentservice.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(CreateOrderEvent event) {

        log.info(
                "Order event received. OrderId={}, UserId={}, Amount={}",
                event.getOrderId(),
                event.getUserId(),
                event.getTotalPrice()
        );

        paymentService.createPayment(event.getOrderId(), event.getUserId(), event.getTotalPrice());

        log.info(
                "Payment created successfully. OrderId={}",
                event.getOrderId()
        );
    }
}