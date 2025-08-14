package com.acelera.fx.digitalsignature.application.usecase.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientFinalStepImplTest {

    @Mock
    private CreateExpedientGetDocumentNamesUseCase getDocumentNames;
    @Mock
    private CreateExpedientGetTitleAndCenterUseCase getTitleAndCenter;
    @Mock
    private CreateExpedientGetClausesUseCase getClauses;
    @Mock
    private CreateExpedientBuildDfdRequestUseCase buildDfdRequest;
    @Mock
    private RestDfdClient restDfdClient;
    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @InjectMocks
    private CreateExpedientFinalStepImpl step;

    private final Locale locale = Locale.getDefault();
    private final String entity = "0049";
    private final Long originId = 123L;
    private final String origin = "TRADE";
    private final String productId = "PROD1";
    private final Long expedientId = 789L;
    
    private CreateExpedientRequest request;
    private TradeSignature tradeSignature;
    private List<TradeSignerDto> signers;
    private List<ProductDocumentParameters> docTypes;
    private List<DocumentSignature> documentSignatures;
    private ExpedientRequest dfdRequest;

    @BeforeEach
    void setUp() {
        request = new CreateExpedientRequest(productId);
        tradeSignature = TradeSignature.builder().tradeSignatureId(456L).build();
        signers = List.of(TradeSignerDto.builder().signerId("SIGNER1").build());
        docTypes = List.of(ProductDocumentParameters.builder().documentType("DOC1").build());
        documentSignatures = List.of(DocumentSignature.builder().documentId("DOC1").build());
        dfdRequest = ExpedientRequest.builder().build();

        setupMocks();
    }

    private void setupMocks() {
        when(getDocumentNames.obtainDocumentSignatures(docTypes, entity, originId, origin))
            .thenReturn(Mono.just(documentSignatures));

        when(getTitleAndCenter.obtainTitleAndCenterData(entity, originId, origin, productId))
            .thenReturn(Mono.just(new String[]{"Title", "Center", "Document"}));

        when(getClauses.obtainClauses(entity, originId, productId))
            .thenReturn(Mono.just("Clauses"));

        when(buildDfdRequest.buildDfdRequest(any(), any(), eq(documentSignatures), 
                eq(request), eq(origin), eq(docTypes), eq(signers), eq(originId)))
            .thenReturn(Mono.just(dfdRequest));

        when(restDfdClient.createExpedient(dfdRequest))
            .thenReturn(Mono.just(expedientId));

        when(tradeSignatureRepositoryClient.save(any()))
            .thenReturn(Mono.just(tradeSignature));
    }

    @Test
    void createExpedient_success() {
        StepVerifier.create(step.createExpedient(docTypes, locale, entity, originId, 
                request, origin, signers, tradeSignature))
            .expectNext(CreateExpedientResponse.builder().expedientId(expedientId).build())
            .verifyComplete();

        verify(restDfdClient).createExpedient(any());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void createExpedient_validateDocumentsFails() {
        documentSignatures = List.of(); // Empty list to trigger validation error

        when(getDocumentNames.obtainDocumentSignatures(docTypes, entity, originId, origin))
            .thenReturn(Mono.just(documentSignatures));

        StepVerifier.create(step.createExpedient(docTypes, locale, entity, originId, 
                request, origin, signers, tradeSignature))
            .expectErrorMatches(e -> e instanceof RuntimeException 
                    && e.getMessage().equals("Algunos documentos no se encontraron."))
            .verify();
    }

    @Test
    void createExpedient_dfdClientFails() {
        when(restDfdClient.createExpedient(any()))
            .thenReturn(Mono.error(new RuntimeException("DFD error")));

        StepVerifier.create(step.createExpedient(docTypes, locale, entity, originId, 
                request, origin, signers, tradeSignature))
            .expectErrorMatches(e -> e.getMessage().contains("DFD calling error"))
            .verify();
    }

    @Test
    void createExpedient_saveTradeSignatureFails() {
        when(tradeSignatureRepositoryClient.save(any()))
            .thenReturn(Mono.error(new RuntimeException("Save error")));

        StepVerifier.create(step.createExpedient(docTypes, locale, entity, originId, 
                request, origin, signers, tradeSignature))
            .expectErrorMatches(e -> e.getMessage().contains("Error guardando tradesignature"))
            .verify();
    }
}