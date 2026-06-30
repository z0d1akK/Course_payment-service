package com.innowise.paymentservice.security.service;

import com.innowise.paymentservice.payment.repository.PaymentRepository;
import com.innowise.paymentservice.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentOwnershipService {

    private final PaymentRepository paymentRepository;

    public boolean isOwnerOrAdmin(UUID paymentId) {
        if (SecurityUtils.isAdmin()) return true;

        return paymentRepository.existsByIdAndUserId(paymentId, SecurityUtils.getCurrentUserId());
    }
}