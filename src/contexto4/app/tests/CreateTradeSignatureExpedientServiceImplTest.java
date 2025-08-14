package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientDocumentStep;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientFinalStep;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientTradeSignatureStep;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTradeSignatureExpedientServiceImplTest {

    @Mock
    private CreateExpedientTradeSignatureStep tradeSignatureStep;

    @Mock
    private CreateExpedientDocumentStep documentStep;

    @Mock
    private CreateExpedientFinalStep expedientStep;

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private CreateTradeSignatureExpedientServiceImpl service;

    private final Locale locale = Locale.getDefault();
    private final String entity = "0049";
    private final Long originId = 123L;
    private final CreateExpedientRequest request = new CreateExpedientRequest("PROD1");

    private final TradeSignature tradeSignature = new TradeSignature();
    private final List<TradeSignerDto> signers = List.of(new TradeSignerDto());
    private final List<ProductDocumentParameters> documentTypes = List.of(new ProductDocumentParameters());
    private final CreateExpedientResponse response = CreateExpedientResponse.builder()
            .expedientId(999L)
            .build();

    @BeforeEach
    void setUp() {
        // Configuración por defecto del origin
        when(tradeSignerHelper.isEventProduct(anyString())).thenReturn(false);

        // Paso 1: TradeSignature
        when(tradeSignatureStep.obtainTradeSignature(entity, originId, request))
                .thenReturn(Mono.just(tradeSignature));

        // Paso 2: Firmantes
        when(tradeSignatureStep.obtainSigners(locale, entity, tradeSignature, originId, "TRADE"))
                .thenReturn(Mono.just(signers));

        // Paso 3: Documentos
        when(documentStep.obtainDocumentTypes(entity, locale, request.getProductId()))
                .thenReturn(Mono.just(documentTypes));

        // Paso 4: Expediente
        when(expedientStep.createExpedient(documentTypes, locale, entity, originId, request, "TRADE", signers, tradeSignature))
                .thenReturn(Mono.just(response));
    }

    @Test
    void createSignatureExpedient_success() {
        StepVerifier.create(service.createSignatureExpedient(locale, entity, originId, request))
                .expectNext(response)
                .verifyComplete();

        verify(tradeSignatureStep).obtainTradeSignature(entity, originId, request);
        verify(tradeSignatureStep).obtainSigners(locale, entity, tradeSignature, originId, "TRADE");
        verify(documentStep).obtainDocumentTypes(entity, locale, request.getProductId());
        verify(expedientStep).createExpedient(documentTypes, locale, entity, originId, request, "TRADE", signers, tradeSignature);
    }

    @Test
    void createSignatureExpedient_errorEnPaso() {
        RuntimeException error = new RuntimeException("Error en paso");
        when(tradeSignatureStep.obtainTradeSignature(entity, originId, request))
                .thenReturn(Mono.error(error));

        StepVerifier.create(service.createSignatureExpedient(locale, entity, originId, request))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Error en paso"))
                .verify();

        verify(tradeSignatureStep).obtainTradeSignature(entity, originId, request);
        verifyNoMoreInteractions(tradeSignatureStep, documentStep, expedientStep);
    }
}


