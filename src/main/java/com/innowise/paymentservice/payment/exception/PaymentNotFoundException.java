package com.innowise.paymentservice.payment.exception;

import com.innowise.paymentservice.common.constants.messages.ErrorMessages;
import com.innowise.paymentservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends ResourceNotFoundException {

    public PaymentNotFoundException(UUID paymentId) {
        super(ErrorMessages.PAYMENT_NOT_FOUND.formatted(paymentId));
    }
}