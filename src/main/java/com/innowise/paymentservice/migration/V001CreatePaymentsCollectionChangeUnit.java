package com.innowise.paymentservice.migration;

import com.innowise.paymentservice.payment.entity.Payment;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

@ChangeUnit(
        id = "create-payments-collection",
        order = "001",
        author = "z0d1akK"
)
@RequiredArgsConstructor
public class V001CreatePaymentsCollectionChangeUnit {

    private final MongoTemplate mongoTemplate;

    @Execution
    public void execute() {

        if (!mongoTemplate.collectionExists(Payment.class)) {
            mongoTemplate.createCollection(Payment.class);
        }
    }

    @RollbackExecution
    public void rollback() {

        if (mongoTemplate.collectionExists(Payment.class)) {
            mongoTemplate.dropCollection(Payment.class);
        }
    }
}