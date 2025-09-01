package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.gateway.domain.dto.response.DocumentLpaResponse;
import com.acelera.broker.gateway.domain.port.DocumentLpaClient;
import com.acelera.fx.digitalsignature.application.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import com.acelera.locale.LocaleConstants;
import com.acelera.locale.MessageSourceHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServicePostImplTest {

    @Mock
    ProductDocumentsService productDocumentsService;

    @Mock
    CreateTradeSignatureExpedientServiceImpl createTradeSignatureExpedientService;

    private @Mock ServerHttpRequest serverHttpRequest;

    private @Mock TradeSignerHelper tradeSignerHelper;
    private @Mock DocumentLpaClient documentLpaClient;

    @InjectMocks
    TradeSignatureServicePostImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final Long ORIGIN_ID = 1L;
    private static final String ERROR_DOCUMENTATION_MESSAGE = "Error de generación de documentación";
    private static final String ERROR_EXPEDIENT_MESSAGE = "Error de generación de expediente";
    private static final String ERROR_FIND_PRODUCT_DOCUMENT = "Id no encontrado";
    private static final String ERROR_POST_CONTRATACION = "Error en Post contratacion";

    @BeforeAll
    static void prepareMessages() {
        LocaleContextHolder.setLocale(LocaleConstants.DEFAULT_LOCALE);

        var ms = new StaticMessageSource();
        ms.addMessage("error.fx.product.document.id.notFound", LocaleConstants.DEFAULT_LOCALE, ERROR_FIND_PRODUCT_DOCUMENT);
        ms.addMessage("error.fx.tradesignature.generacion.documentos", LocaleConstants.DEFAULT_LOCALE, ERROR_DOCUMENTATION_MESSAGE);
        ms.addMessage("error.fx.tradesignature.generacion.expediente", LocaleConstants.DEFAULT_LOCALE, ERROR_EXPEDIENT_MESSAGE);
        ms.addMessage("error.fx.tradesignature.post-contratacion", LocaleConstants.DEFAULT_LOCALE, ERROR_POST_CONTRATACION);
        MessageSourceHolder.setMessageSource(ms);
    }

    @BeforeEach
    void setUp() {
        ProductDocumentParameters doc = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        when(productDocumentsService.findProductDocumentType(any(), any(), any()))
                .thenReturn(Flux.just(doc));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer test-token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);
    }

    private StartSignatureRequestDto createTestRequest() {
        return PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);
    }

    private Mono<StartSignatureResponseDto> executeWorkflow(Long originId) {
        return impl.startSignatureWorkflow(
                LocaleConstants.ENTITY_0049,
                LocaleConstants.DEFAULT_LOCALE,
                originId,
                createTestRequest(),
                serverHttpRequest);
    }

    private void mockCreateExpedient(CreateExpedientResponse response) {
        when(createTradeSignatureExpedientService.createSignatureExpedient(
                any(Locale.class),
                any(String.class),
                any(Long.class),
                any(CreateExpedientRequest.class)))
                .thenReturn(Mono.just(response));
    }

    private void mockCreateDocument(DocumentLpaResponse document) {
        when(documentLpaClient.generateDocumentLpa(any())).thenReturn(Mono.just(document));
    }

    private void mockCreateExpedientError() {
        when(createTradeSignatureExpedientService.createSignatureExpedient(
                any(Locale.class),
                any(String.class),
                any(Long.class),
                any(CreateExpedientRequest.class)))
                .thenReturn(Mono.error(new RuntimeException(ERROR_EXPEDIENT_MESSAGE)));
    }

    @Test
    void testStartSignatureWorkflow_ok() {
        // Given
        CreateExpedientResponse expedient = PODAM_FACTORY.manufacturePojo(CreateExpedientResponse.class);
        mockCreateExpedient(expedient);

        DocumentLpaResponse document = DocumentLpaResponse.builder().build();
        mockCreateDocument(document);

        // When/Then
        when(tradeSignerHelper.isEventProduct(any())).thenReturn(true);
        when(documentLpaClient.generateDocumentLpa(any())).thenReturn(Mono.just(document));

        StepVerifier.create(executeWorkflow(ORIGIN_ID))
                .expectNextMatches(resp -> resp.getExpedientId() != null)
                .verifyComplete();
    }



    @Test
    void testStartSignatureWorkflow_error_generateDocumentLpa() {
        when(documentLpaClient.generateDocumentLpa(any()))
                .thenReturn(Mono.error(new RuntimeException(ERROR_DOCUMENTATION_MESSAGE)));

        // When/Then
        StepVerifier.create(executeWorkflow(301L))
                .expectErrorMatches(e -> e.getMessage().contains(ERROR_DOCUMENTATION_MESSAGE))
                .verify();
    }

    @Test
    void testStartSignatureWorkflow_error_createSignatureExpedient() {
        // Given
        DocumentLpaResponse document = DocumentLpaResponse.builder().build();
        mockCreateDocument(document);
        mockCreateExpedientError();

        // When/Then
        StepVerifier.create(executeWorkflow(ORIGIN_ID))
                .expectErrorMatches(e -> e.getMessage().contains(ERROR_EXPEDIENT_MESSAGE))
                .verify();
    }

    @Test
    void testStartSignatureWorkflow_error_productDocument_NotFound() {
        // Given
        when(productDocumentsService.findProductDocumentType(any(), any(), any()))
                .thenReturn(Flux.empty());

        // When/Then
        StepVerifier.create(executeWorkflow(ORIGIN_ID))
                .expectErrorMatches(e -> e.getMessage().contains(ERROR_FIND_PRODUCT_DOCUMENT))
                .verify();
    }
}
