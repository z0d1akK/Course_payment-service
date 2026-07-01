package com.innowise.paymentservice.payment.repository;

import com.innowise.paymentservice.payment.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID>, PaymentCustomRepository {
}