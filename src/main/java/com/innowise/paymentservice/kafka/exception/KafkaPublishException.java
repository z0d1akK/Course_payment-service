package com.innowise.paymentservice.kafka.exception;

import com.innowise.paymentservice.common.exception.BusinessException;

public class KafkaPublishException extends BusinessException {

    public KafkaPublishException(String message) {
        super(message);
    }
}