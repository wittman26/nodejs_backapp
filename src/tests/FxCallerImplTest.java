package com.acelera.rest.fx.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

class FxCallerImplTest {

    static MockWebServer mockBackEnd;
    FxCallerImpl fxCaller;
    static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String TOKEN = "test-token";

    @BeforeAll
    static void setUp() throws Exception {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws Exception {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void init() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        fxCaller = new FxCallerImpl(webClient);
    }

    @Test
    void testUpdateTradeSignature_ok() throws Exception {
        TradeSignatureResponse response = PODAM_FACTORY.manufacturePojo(TradeSignatureResponse.class);
        String mockResponse = OBJECT_MAPPER.writeValueAsString(response);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        TradeSignatureRequest request = new TradeSignatureRequest();
        request.setToken(TOKEN);
        // set other fields as needed

        fxCaller.updateTradeSignature(null, null, request)
                .as(StepVerifier::create)
                .assertNext(resp -> assertEquals(response.getTradeSignatureId(), resp.getTradeSignatureId()))
                .verifyComplete();

        var recordedRequest = mockBackEnd.takeRequest();
        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals("/v1/trades-signatures", recordedRequest.getPath());
        assertEquals(TOKEN, recordedRequest.getHeader("Authorization"));
    }

    @Test
    void testGetTradeSignature_ok() throws Exception {
        GetTradeSignatureResponse response = PODAM_FACTORY.manufacturePojo(GetTradeSignatureResponse.class);
        String mockResponse = OBJECT_MAPPER.writeValueAsString(response);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        GetTradeSignatureRequestParameter request = new GetTradeSignatureRequestParameter();
        request.setToken(TOKEN);
        request.setTradeSignatureId(response.getTradeSignatureId());
        // set other fields as needed

        fxCaller.getTradeSignature(null, null, request)
                .as(StepVerifier::create)
                .assertNext(resp -> assertEquals(response.getTradeSignatureId(), resp.getTradeSignatureId()))
                .verifyComplete();

        var recordedRequest = mockBackEnd.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(TOKEN, recordedRequest.getHeader("Authorization"));
    }

    @Test
    void testUpdateTradeSignature_error() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Error"));

        TradeSignatureRequest request = new TradeSignatureRequest();
        request.setToken(TOKEN);

        fxCaller.updateTradeSignature(null, null, request)
                .as(StepVerifier::create)
                .expectErrorMatches(e -> e.getMessage().contains("500"))
                .verify();
    }
}
