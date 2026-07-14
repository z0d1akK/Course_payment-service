package com.innowise.paymentservice.client.random.feign;

import com.innowise.paymentservice.client.random.exception.RandomApiException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class RandomApiErrorDecoder implements ErrorDecoder {

    private static final String NOT_FOUND = "Random API endpoint not found";

    private static final String RATE_LIMIT_EXCEEDED = "Random API rate limit exceeded";

    private static final String RANDOM_API_SERVICE_UNAVAILABLE = "Random API service unavailable";

    private static final String RANDOM_API_RETURNED_STATUS = "Random API returned status: ";

    @Override
    public Exception decode(String methodKey, Response response) {

        return switch (response.status()) {
            case 404 -> new RandomApiException(NOT_FOUND);
            case 429 -> new RandomApiException(RATE_LIMIT_EXCEEDED);
            case 503 -> new RandomApiException(RANDOM_API_SERVICE_UNAVAILABLE);
            default -> new RandomApiException(RANDOM_API_RETURNED_STATUS + response.status());
        };
    }
}