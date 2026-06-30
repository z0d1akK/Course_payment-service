package com.innowise.paymentservice.common.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class RandomApiWireMockStub {

    private static final String RANDOM_ENDPOINT = "/integers/?num=1&min=0&max=100&col=1&base=10&format=plain&rnd=new";

    private RandomApiWireMockStub() {
    }

    public static void stubEvenNumber(WireMockServer wireMockServer) {
        wireMockServer.stubFor(get(urlEqualTo(RANDOM_ENDPOINT))
                .willReturn(ok("42"))
        );
    }

    public static void stubOddNumber(WireMockServer wireMockServer) {
        wireMockServer.stubFor(get(urlEqualTo(RANDOM_ENDPOINT))
                .willReturn(ok("17"))
        );
    }
}