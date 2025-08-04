package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceSave;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateExpedientResponse;
import com.acelera.locale.LocaleConstants;
import com.acelera.locale.MessageSourceHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
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
    TradeSignatureServiceSave tradeSignatureServiceSave;

    @InjectMocks
    TradeSignatureServicePostImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final Long ORIGIN_ID = 1L;

    private static final String ERROR_DOCUMENTATION_MESSAGE = "Error de generación de documentación";

    private static final String ERROR_EXPEDIENT_MESSAGE = "Error de generación de expediente";

    @BeforeAll
    static void prepareMessages() {
        LocaleContextHolder.setLocale(LocaleConstants.DEFAULT_LOCALE);

        var ms = new StaticMessageSource();
        ms.addMessage("error.fx.product.document.id.notFound", LocaleConstants.DEFAULT_LOCALE, "Id no encontrado");
        ms.addMessage("error.fx.tradesignature.generacion.documentos", LocaleConstants.DEFAULT_LOCALE, ERROR_DOCUMENTATION_MESSAGE);
        ms.addMessage("error.fx.tradesignature.generacion.expediente", LocaleConstants.DEFAULT_LOCALE, ERROR_EXPEDIENT_MESSAGE);
        MessageSourceHolder.setMessageSource(ms);
    }

    @Test
    void testStartSignatureWorkflow_ok() {

        ProductDocumentParameters doc = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        when(productDocumentsService.findProductDocumentType(any(String.class), any(Locale.class), any(String.class)))
                .thenReturn(Flux.just(doc));

        CreateExpedientResponse expedient = PODAM_FACTORY.manufacturePojo(CreateExpedientResponse.class);

        when(tradeSignatureServiceSave.createSignatureExpedient(
                        any(Locale.class),
                        any(String.class),
                        any(Long.class),
                        any(CreateExpedientRequest.class)))
                .thenReturn(Mono.just(expedient));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);
        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, ORIGIN_ID, request);

        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getExpedientId() != null)
                .verifyComplete();
    }

    @Test
    void testStartSignatureWorkflow_error_generacionDocumentacion() {
        ProductDocumentParameters doc = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        when(productDocumentsService.findProductDocumentType(any(String.class), any(Locale.class), any(String.class)))
                .thenReturn(Flux.just(doc));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);

        // originId >300 para simular error en documentación
        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, 301L, request);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains(ERROR_DOCUMENTATION_MESSAGE))
                .verify();
    }

    @Test
    void testStartSignatureWorkflow_error_generacionExpediente() {
        ProductDocumentParameters doc = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        when(productDocumentsService.findProductDocumentType(any(String.class), any(Locale.class), any(String.class)))
                .thenReturn(Flux.just(doc));

        when(tradeSignatureServiceSave.createSignatureExpedient(
                any(Locale.class),
                any(String.class),
                any(Long.class),
                any(CreateExpedientRequest.class)))
                .thenReturn(Mono.error(new RuntimeException(ERROR_EXPEDIENT_MESSAGE)));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);

        // originId negativo para simular error en expediente
        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, -1L, request);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains(ERROR_EXPEDIENT_MESSAGE))
                .verify();
    }
}
