package com.innowise.paymentservice.payment.integration;

import com.innowise.paymentservice.common.AbstractIntegrationTest;
import com.innowise.paymentservice.common.annotation.WithMockCustomUser;
import com.innowise.paymentservice.common.kafka.KafkaConsumerFactory;
import com.innowise.paymentservice.common.kafka.KafkaTestConsumer;
import com.innowise.paymentservice.common.wiremock.RandomApiWireMockStub;
import com.innowise.paymentservice.kafka.event.PaymentCompletedEvent;
import com.innowise.paymentservice.payment.dto.request.CreatePaymentRequestDto;
import com.innowise.paymentservice.payment.entity.Payment;
import com.innowise.paymentservice.payment.entity.PaymentStatus;
import com.innowise.paymentservice.payment.repository.PaymentRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.Instant;

import static com.innowise.paymentservice.payment.testclasses.PaymentTestDataFactory.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PaymentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String TOPIC = "payment-events";

    @Autowired
    private PaymentRepository paymentRepository;

    private KafkaTestConsumer<PaymentCompletedEvent> kafkaConsumer;

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

        kafkaConsumer.subscribe(TOPIC);
    }

    @AfterEach
    void tearDown() {
        kafkaConsumer.close();
    }

    @Test
    @DisplayName("Should create payment successfully")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void createPayment_ShouldCreatePayment() throws Exception {
        RandomApiWireMockStub.stubEvenNumber(wireMockServer);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Payment payment = paymentRepository.findAll().getFirst();
                    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
                });

        Payment payment = paymentRepository.findAll().getFirst();

        PaymentCompletedEvent event = kafkaConsumer.receive();

        assertThat(event.getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Should return 400 when request invalid")
    @WithMockCustomUser
    void createPayment_WhenInvalidRequest_ShouldReturn400() throws Exception {

        CreatePaymentRequestDto request = CreatePaymentRequestDto.builder()
                .build();

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should process payment as failed")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void createPayment_WhenRandomReturnsOdd_ShouldProcessFailed() throws Exception {

        RandomApiWireMockStub.stubOddNumber(wireMockServer);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentRequest())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Payment payment = paymentRepository.findAll().getFirst();
                    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
                });

        Payment payment = paymentRepository.findAll().getFirst();

        PaymentCompletedEvent event = kafkaConsumer.receive();

        assertThat(event.getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should return payment by id")
    @WithMockCustomUser(
            userId = "33333333-3333-3333-3333-333333333333"
    )
    void getPaymentById_ShouldReturnPayment() throws Exception {

        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentAmount").value(150));
    }

    @Test
    @DisplayName("Should return 403 when user requests other payment")
    @WithMockCustomUser(
            userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    )
    void getPaymentById_WhenNotOwner_ShouldReturn403() throws Exception {

        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to get any payment")
    @WithMockCustomUser(role = "ROLE_ADMIN")
    void getPaymentById_WhenAdmin_ShouldReturnPayment() throws Exception {

        Payment payment = paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments/{id}", payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()));
    }

    @Test
    @DisplayName("Should return 404 when payment not found")
    @WithMockCustomUser
    void getPaymentById_WhenNotFound_ShouldReturn404() throws Exception {

        mockMvc.perform(get("/api/payments/{id}", PAYMENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return payments")
    @WithMockCustomUser(
            userId = "33333333-3333-3333-3333-333333333333"
    )
    void getPayments_ShouldReturnPayments() throws Exception {

        paymentRepository.save(createSuccessfulPayment());

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.content[0].orderId").value(ORDER_ID.toString()));
    }

    @Test
    @DisplayName("Should return user summary")
    @WithMockCustomUser(userId = "33333333-3333-3333-3333-333333333333")
    void getUserSummary_ShouldReturnSummary() throws Exception {

        Payment payment = createSuccessfulPayment();
        payment.setTimestamp(Instant.now());
        paymentRepository.save(payment);

        Instant now = Instant.now();
        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
                                .param("from", from)
                                .param("to", to)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(150));
    }

    @Test
    @DisplayName("Should return 403 when user requests other summary")
    @WithMockCustomUser(
            userId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    )
    void getUserSummary_WhenNotOwner_ShouldReturn403() throws Exception {

        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
                                .param("from", "2026-01-01T00:00:00Z")
                                .param("to", "2026-12-31T23:59:59Z")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow admin to get any user summary")
    @WithMockCustomUser(role = "ROLE_ADMIN")
    void getUserSummary_WhenAdmin_ShouldReturnSummary() throws Exception {
        Payment payment = createSuccessfulPayment();
        payment.setTimestamp(Instant.now());
        paymentRepository.save(payment);

        Instant now = Instant.now();
        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/users/{userId}/summary", USER_ID)
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
        Payment payment = createSuccessfulPayment();

        payment.setTimestamp(Instant.now());
        paymentRepository.save(payment);

        Instant now = Instant.now();
        String from = now.minusSeconds(3600).toString();
        String to = now.plusSeconds(3600).toString();

        mockMvc.perform(
                        get("/api/payments/summary")
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
                                .param("from", "2026-01-01T00:00:00Z")
                                .param("to", "2026-12-31T23:59:59Z")
                )
                .andExpect(status().isForbidden());
    }

}