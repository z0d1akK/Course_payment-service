package com.innowise.paymentservice.payment.repository.impl;

import com.innowise.paymentservice.common.constants.DocumentFields;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.repository.PaymentCustomRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Payment> findPayments(UUID userId, UUID orderId, PaymentStatus status) {

        Criteria criteria = new Criteria();

        if (userId != null) {
            criteria.and(DocumentFields.USER_ID).is(userId);
        }

        if (orderId != null) {
            criteria.and(DocumentFields.ORDER_ID).is(orderId);
        }

        if (status != null) {
            criteria.and(DocumentFields.STATUS).is(status);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.sort(Sort.Direction.DESC, DocumentFields.TIMESTAMP)
        );

        return mongoTemplate.aggregate(aggregation, Payment.class, Payment.class).getMappedResults();
    }

    @Override
    public BigDecimal calculateUserPaymentsSum(UUID userId, Instant from, Instant to) {

        MatchOperation match = Aggregation.match(
                Criteria.where(DocumentFields.USER_ID).is(userId)
                        .and(DocumentFields.STATUS).is(PaymentStatus.SUCCESS)
                        .and(DocumentFields.TIMESTAMP).gte(from).lte(to)
        );

        return getTotalPaymentAmount(match);
    }

    @NotNull
    private BigDecimal getTotalPaymentAmount(MatchOperation match) {
        GroupOperation group = Aggregation.group()
                .sum(DocumentFields.PAYMENT_AMOUNT)
                .as("total");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        Document result = mongoTemplate.aggregate(aggregation, Payment.class, Document.class)
                .getUniqueMappedResult();

        if (result == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(result.get("total").toString());
    }

    @Override
    public BigDecimal calculateAllPaymentsSum(Instant from, Instant to) {

        MatchOperation match = Aggregation.match(
                Criteria.where(DocumentFields.STATUS).is(PaymentStatus.SUCCESS)
                        .and(DocumentFields.TIMESTAMP).gte(from).lte(to)
        );

        return getTotalPaymentAmount(match);
    }
}