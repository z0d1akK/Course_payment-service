package com.innowise.paymentservice.client.random.service;

import com.innowise.paymentservice.client.random.exception.RandomApiException;
import com.innowise.paymentservice.client.random.feign.RandomApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomApiService {

    private static final int MIN_VALUE = 0;

    private static final int MAX_VALUE = 100;

    public static final String EMPTY_RESPONSE_FROM_RANDOM_API = "Empty response from Random API";

    public static final String INVALID_RESPONSE_RECEIVED_FROM_RANDOM_API = "Invalid response received from Random API";

    private final RandomApiClient randomApiClient;

    public int getRandomNumber() {
        String response = randomApiClient.getRandomNumber(1, MIN_VALUE, MAX_VALUE, 1, 10, "plain", "new");

        try {
            String value = response.trim();
            if (value.isBlank()) {
                throw new RandomApiException(EMPTY_RESPONSE_FROM_RANDOM_API);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new RandomApiException(INVALID_RESPONSE_RECEIVED_FROM_RANDOM_API);
        }
    }

    public boolean isEvenNumber() {
        return getRandomNumber() % 2 == 0;
    }
}