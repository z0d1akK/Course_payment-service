package com.innowise.paymentservice.kafka.config;

import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PaymentCompletedEvent> producerFactory(KafkaProperties kafkaProperties) {

        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildProducerProperties());

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate(ProducerFactory<String, PaymentCompletedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}