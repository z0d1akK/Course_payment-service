package com.innowise.paymentservice.client.random.feign;

import com.innowise.paymentservice.client.random.config.RandomApiFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "random-api",
        url = "${payment.random-api.url}",
        configuration = RandomApiFeignConfig.class
)
public interface RandomApiClient {

    @GetMapping("/integers/")
    String getRandomNumber(
            @RequestParam("num") int num,
            @RequestParam("min") int min,
            @RequestParam("max") int max,
            @RequestParam("col") int col,
            @RequestParam("base") int base,
            @RequestParam("format") String format,
            @RequestParam("rnd") String rnd
    );
}