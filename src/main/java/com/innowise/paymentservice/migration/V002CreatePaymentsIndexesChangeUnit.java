package com.innowise.paymentservice.migration;

import com.innowise.paymentservice.common.constants.DocumentFields;
import com.innowise.paymentservice.payment.entity.Payment;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@ChangeUnit(
        id = "create-payments-indexes",
        order = "002",
        author = "z0d1akK"
)
@RequiredArgsConstructor
public class V002CreatePaymentsIndexesChangeUnit {

    private final MongoTemplate mongoTemplate;

    @Execution
    public void execute() {

        IndexOperations indexes = mongoTemplate.indexOps(Payment.class);

        indexes.createIndex(
                new Index()
                        .on(DocumentFields.ORDER_ID, Sort.Direction.ASC)
                        .named("idx_payment_order_id")
        );

        indexes.createIndex(
                new Index()
                        .on(DocumentFields.USER_ID, Sort.Direction.ASC)
                        .named("idx_payment_user_id")
        );

        indexes.createIndex(
                new Index()
                        .on(DocumentFields.STATUS, Sort.Direction.ASC)
                        .named("idx_payment_status")
        );

        indexes.createIndex(
                new Index()
                        .on(DocumentFields.TIMESTAMP, Sort.Direction.ASC)
                        .named("idx_payment_timestamp")
        );

        indexes.createIndex(
                new Index()
                        .on(DocumentFields.USER_ID, Sort.Direction.ASC)
                        .on(DocumentFields.TIMESTAMP, Sort.Direction.ASC)
                        .named("idx_payment_user_timestamp")
        );

        indexes.createIndex(
                new Index()
                        .on("status", Sort.Direction.ASC)
                        .on("timestamp", Sort.Direction.ASC)
                        .named("idx_payment_status_timestamp")
        );
    }

    @RollbackExecution
    public void rollback() { }
}