package com.innowise.paymentservice.client.random.exception;

import com.innowise.paymentservice.common.exception.BusinessException;

public class RandomApiException extends BusinessException {

    public RandomApiException(String message) {
        super(message);
    }
}