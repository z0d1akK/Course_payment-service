package com.innowise.paymentservice.client.random.config;

import com.innowise.paymentservice.client.random.feign.RandomApiErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomApiFeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new RandomApiErrorDecoder();
    }
}