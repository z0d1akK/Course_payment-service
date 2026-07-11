package com.innowise.paymentservice.payment.integration;

import com.innowise.paymentservice.common.AbstractIntegrationTest;
import com.innowise.paymentservice.common.annotation.WithMockCustomUser;
import com.innowise.paymentservice.common.kafka.KafkaConsumerFactory;
import com.innowise.paymentservice.common.kafka.KafkaProducerFactory;
import com.innowise.paymentservice.common.kafka.KafkaTestConsumer;
import com.innowise.paymentservice.common.wiremock.RandomApiWireMockStub;
import com.innowise.paymentservice.kafka.event.CreateOrderEvent;
import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import com.innowise.paymentservice.kafka.properties.KafkaTopics;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.innowise.paymentservice.payment.testclasses.PaymentTestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PaymentControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private KafkaTestConsumer<PaymentCompletedEvent> kafkaConsumer;

    @Value("${gateway.api-key}")
    private String gatewayApiKey;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        kafkaConsumer = new KafkaTestConsumer<>(
                KafkaConsumerFactory.createConsumer(
                        kafkaContainer,
                        PaymentCompletedEvent.class,
                        "payment-test"
                )
        );

        kafkaConsumer.subscribe(KafkaTopics.PAYMENT_COMPLETED);
    }

    @AfterEach
    void tearDown() {
        kafkaConsumer.close();
    }

    @Test
    @DisplayName("Should process CreateOrderEvent, call RandomApi and publish PaymentCompletedEvent with SUCCESS")
    void createPayment_WhenOrderEventReceived_ShouldProcessPaymentSuccessfully() throws Exception {
        RandomApiWireMockStub.stubEvenNumber(wireMockServer);

        UUID orderId = UUID.randomUUID();
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(orderId)
                .userId(USER_ID)
                .totalPrice(new BigDecimal("250.00"))
                .build();

        KafkaTemplate<String, CreateOrderEvent> kafkaTemplate =
                KafkaProducerFactory.createKafkaTemplate(kafkaContainer);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderId.toString(), event)
                .get(10, TimeUnit.SECONDS);

        PaymentCompletedEvent paymentEvent = kafkaConsumer.receive();

        assertThat(paymentEvent.getOrderId()).isEqualTo(orderId);
        assertThat(paymentEvent.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> paymentRepository.findAll().stream()
                        .anyMatch(p -> p.getOrderId().equals(orderId)
                                && p.getStatus() == PaymentStatus.SUCCESS));

        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow();

        assertThat(payment.getPaymentAmount())
                .isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Should process CreateOrderEvent and publish PaymentCompletedEvent with FAILED when RandomApi returns odd")
    void createPayment_WhenRandomApiReturnsOdd_ShouldPublishFailedEvent() throws Exception {
        RandomApiWireMockStub.stubOddNumber(wireMockServer);

        UUID orderId = UUID.randomUUID();
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(orderId)
                .userId(USER_ID)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        KafkaTemplate<String, CreateOrderEvent> kafkaTemplate =
                KafkaProducerFactory.createKafkaTemplate(kafkaContainer);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderId.toString(), event)
                .get(10, TimeUnit.SECONDS);

        PaymentCompletedEvent paymentEvent = kafkaConsumer.receive();

        assertThat(paymentEvent.getOrderId()).isEqualTo(orderId);
        assertThat(paymentEvent.getStatus()).isEqualTo(PaymentStatus.FAILED);

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> paymentRepository.findAll().stream()
                        .anyMatch(p -> p.getOrderId().equals(orderId)
                                && p.getStatus() == PaymentStatus.FAILED));

        Payment payment = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getPaymentAmount())
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should return payment by id")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void getPaymentById_ShouldReturnPayment() throws Exception {
        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId())
                        .header("X-Gateway-Key", gatewayApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentAmount").value(150));
    }

    @Test
    @DisplayName("Should return 403 when user requests other payment")
    @WithMockCustomUser(userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void getPaymentById_WhenNotOwner_ShouldReturn403() throws Exception {
        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId())
                        .header("X-Gateway-Key", gatewayApiKey))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to get any payment")
    @WithMockCustomUser(role = "ROLE_ADMIN")
    void getPaymentById_WhenAdmin_ShouldReturnPayment() throws Exception {
        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId())
                        .header("X-Gateway-Key", gatewayApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()));
    }

    @Test
    @DisplayName("Should return 404 when payment not found")
    @WithMockCustomUser
    void getPaymentById_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", PAYMENT_ID)
                        .header("X-Gateway-Key", gatewayApiKey))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return payments")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void getPayments_ShouldReturnPayments() throws Exception {
        paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments")
                        .header("X-Gateway-Key", gatewayApiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.content[0].orderId").value(ORDER_ID.toString()));
    }

    @Test
    @DisplayName("Should return user summary")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void getUserSummary_ShouldReturnSummary() throws Exception {
        Instant now = Instant.now();

        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(now);
        paymentRepository.save(payment);

        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
                                .header("X-Gateway-Key", gatewayApiKey)
                                .param("from", from)
                                .param("to", to)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150));
    }

    @Test
    @DisplayName("Should return 403 when user requests other summary")
    @WithMockCustomUser(userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    void getUserSummary_WhenNotOwner_ShouldReturn403() throws Exception {
        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
                                .header("X-Gateway-Key", gatewayApiKey)
                                .param("from", "2026-01-01T00:00:00Z")
                                .param("to", "2026-12-31T23:59:59Z")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to get any user summary")
    @WithMockCustomUser(role = "ROLE_ADMIN")
    void getUserSummary_WhenAdmin_ShouldReturnSummary() throws Exception {
        Instant now = Instant.now();

        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(now);
        paymentRepository.save(payment);

        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
                                .header("X-Gateway-Key", gatewayApiKey)
                                .param("from", from)
                                .param("to", to)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150));
    }

    @Test
    @DisplayName("Should return all summary")
    @WithMockCustomUser(role = "ROLE_ADMIN")
    void getAllSummary_ShouldReturnSummary() throws Exception {
        Instant now = Instant.now();

        Payment payment = createPayment();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTimestamp(now);
        paymentRepository.save(payment);

        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/summary")
                                .header("X-Gateway-Key", gatewayApiKey)
                                .param("from", from)
                                .param("to", to)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150));
    }

    @Test
    @DisplayName("Should return 403 when user requests platform summary")
    @WithMockCustomUser
    void getAllSummary_WhenUser_ShouldReturn403() throws Exception {
        mockMvc.perform(
                        get("/api/payments/summary")
                                .header("X-Gateway-Key", gatewayApiKey)
                                .param("from", "2026-01-01T00:00:00Z")
                                .param("to", "2026-12-31T23:59:59Z")
                )
                .andExpect(status().isForbidden());
    }

}