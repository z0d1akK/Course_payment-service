package com.innowise.paymentservice.kafka.producer;

import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import com.innowise.paymentservice.kafka.exception.KafkaPublishException;
import com.innowise.paymentservice.kafka.properties.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            CompletableFuture<SendResult<String, PaymentCompletedEvent>>
                    future = kafkaTemplate.send(
                    KafkaTopics.PAYMENT_EVENTS,
                    event.getOrderId().toString(),
                    event
            );

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error(
                            "Failed to publish payment event. OrderId={}, status={}",
                            event.getOrderId(),
                            event.getStatus(),
                            exception
                    );
                    return;
                }
                log.info(
                        "Payment event published successfully. OrderId={}, status={}, partition={}, offset={}",
                        event.getOrderId(),
                        event.getStatus(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            });

        } catch (Exception exception) {
            throw new KafkaPublishException(String.format(
                    "Failed to publish payment event for order %s",
                    event.getOrderId())
            );
        }
    }
}