package com.innowise.paymentservice.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pending"),
    SUCCESS("Success"),
    FAILED("Failed");

    private final String statusName;

    PaymentStatus(String statusName) {
        this.statusName = statusName;
    }
}