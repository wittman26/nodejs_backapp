package com.acelera.rest.ffvvquery.infrastructure.adapter.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;

import com.acelera.rest.ffvvquery.infrastructure.config.FfvvQueryConfig;
import com.acelera.rest.idp.san.domain.port.external.SasSanCaller;
import com.acelera.web.WebClientAutoConfig;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = { FfvvQueryConfig.class, WebClientAutoConfiguration.class,
        WebClientAutoConfig.class }, properties = { "logging.level.reactor.netty.http.client.HttpClient=debug" })
class FfvvQueryCallerImplTest {
    private static final int PORT = ThreadLocalRandom.current().nextInt(1111, 9999);

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("rest.ffvvquery.url", () -> "http://localhost:" + PORT);
    }

    public static MockWebServer mockBackEnd;

    private @Autowired FfvvQueryCallerImpl impl;
    private @MockitoBean SasSanCaller sasClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start(PORT);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void testFind() throws IOException, InterruptedException {
        var mockResponse = Files.readString(Paths.get("src/test/resources", "ffvvquery/ffvvquery-find-response.json"));
        mockBackEnd.enqueue(new MockResponse().setBody(mockResponse).addHeader(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE));

        when(sasClient.generateJwtTokenForSanBce(any(), any())).thenReturn(Mono.just("Token"));

        var isin = "ES0813211010";
        impl.find(isin).as(StepVerifier::create).expectNext("1PN000182003").verifyComplete();

        var recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(UriComponentsBuilder.fromUriString("/").queryParam("activeCode", "ES0813211010")
                .queryParam("typeCode", "ISIN").queryParam("typeCodeResponse", "SIGA").queryParam("entity", "9999")
                .build().toString(), recordedRequest.getPath());
    }
}
