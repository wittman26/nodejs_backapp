package com.acelera.rest.microlito.infrastructure;

import com.acelera.broker.microlito.domain.dto.request.DocumentLpaCreateRequest;
import com.acelera.broker.microlito.domain.dto.response.DocumentLpaResponse;
import com.acelera.rest.microlito.MicrolitoCallerImpl;
import com.acelera.rest.microlito.domain.port.MicrolitoCaller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.sql.Timestamp;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicrolitoCallerImplTest {

    static MockWebServer mockBackEnd;
    MicrolitoCaller microlitoCaller;
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
        mockBackEnd = new MockWebServer();
        microlitoCaller = new MicrolitoCallerImpl(WebClient.builder()
                .baseUrl(String.format("http://localhost:%s", mockBackEnd.getPort()))
                .build());
    }

    private<T> void enqueueMockResponse(T response) throws Exception {
        String mockResponse = OBJECT_MAPPER.writeValueAsString(response);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    }

    private void verifyHttpRequest(String expectedMethod, String expectedPath) throws InterruptedException {
        var recordedRequest = mockBackEnd.takeRequest();
        assertEquals(expectedMethod, recordedRequest.getMethod());
        assertEquals(expectedPath, recordedRequest.getPath());
        assertEquals(TOKEN, Objects.requireNonNull(recordedRequest.getHeader("Authorization"))
                .replaceFirst("(?i)^Bearer","").trim());
    }

    @Test
    void testGenerateDocumentLpa_ok() throws Exception {
        // Create response manually
        DocumentLpaResponse response = DocumentLpaResponse.builder()
                .idOperacion(1)
                .idOrden(1)
                .idEvento(1)
                .entidad("0049")
                .nombreDocumento("test.pdf")
                .descripcionDocumento("Test Document")
                .sentido("E")
                .idDocumentoLpa("DOC1")
                .idLpa(1)
                .datos("test data")
                .tipoDocumento(new DocumentLpaResponse.ComboBean("DESC1", "es", "TYPE1", "1", null))
                .tipoProducto(new DocumentLpaResponse.ComboBean("PROD1", "es", "TYPE1", "1", null))
                .alta(new DocumentLpaResponse.ComboUsuarioFecha("USER1", new Timestamp(System.currentTimeMillis())))
                .modif(new DocumentLpaResponse.ComboUsuarioFecha("USER2", new Timestamp(System.currentTimeMillis())))
                .build();

        enqueueMockResponse(response);

        DocumentLpaCreateRequest request = new DocumentLpaCreateRequest();
        request.setToken(TOKEN);

        microlitoCaller.generateDocumentLpa(request)
                .as(StepVerifier::create)
                .assertNext(resp -> assertEquals(response.getDescripcionDocumento(), resp.getDescripcionDocumento()))
                .verifyComplete();

        verifyHttpRequest("POST", "/acelera-api/fx/v1/micro/trades-signatures/{originId}/documents");
    }

    @Test
    void testGenerateDocumentLpa_error() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Error"));

        DocumentLpaCreateRequest request = new DocumentLpaCreateRequest();
        request.setToken(TOKEN);

        microlitoCaller.generateDocumentLpa(request)
                .as(StepVerifier::create)
                .expectErrorMatches(e -> e.getMessage().contains("500"))
                .verify();
    }
}
