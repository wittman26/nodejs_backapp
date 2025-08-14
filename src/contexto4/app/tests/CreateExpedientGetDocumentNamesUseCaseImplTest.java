package com.acelera.fx.digitalsignature.application.usecase.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.acelera.broker.fx.db.domain.dto.DocumentRequest;
import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.port.DocumentSignatureRepositoryClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientGetDocumentNamesUseCaseImplTest {

    @Mock
    private DocumentSignatureRepositoryClient documentSignatureRepositoryClient;

    @InjectMocks
    private CreateExpedientGetDocumentNamesUseCaseImpl useCase;

    private final String entity = "0049";
    private final Long originId = 123L;
    private final String documentType = "DOC1";

    private ProductDocumentParameters documentParameter;
    private DocumentSignature documentSignature;
    private List<ProductDocumentParameters> documentTypes;

    @BeforeEach
    void setUp() {
        documentParameter = ProductDocumentParameters.builder()
                .documentType(documentType)
                .build();
        
        documentSignature = DocumentSignature.builder()
                .documentId("DOC1")
                .documentType(documentType)
                .build();

        documentTypes = List.of(documentParameter);

        // Setup default mock responses
        when(documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(any()))
                .thenReturn(Mono.just(documentSignature));
        
        when(documentSignatureRepositoryClient.findByEntityAndEventAndDocumentType(any()))
                .thenReturn(Mono.just(documentSignature));
    }

    @Test
    void obtainDocumentSignatures_tradePath_success() {
        StepVerifier.create(useCase.obtainDocumentSignatures(documentTypes, entity, originId, "TRADE"))
                .expectNext(List.of(documentSignature))
                .verifyComplete();

        DocumentRequest expectedRequest = DocumentRequest.builder()
                .entityId(entity)
                .documentTypeId(documentType)
                .operationId(originId)
                .eventId(null)
                .build();

        verify(documentSignatureRepositoryClient)
                .findByEntityAndOperationAndDocumentType(expectedRequest);
    }

    @Test
    void obtainDocumentSignatures_eventPath_success() {
        StepVerifier.create(useCase.obtainDocumentSignatures(documentTypes, entity, originId, "EVENT"))
                .expectNext(List.of(documentSignature))
                .verifyComplete();

        DocumentRequest expectedRequest = DocumentRequest.builder()
                .entityId(entity)
                .documentTypeId(documentType)
                .operationId(null)
                .eventId(originId)
                .build();

        verify(documentSignatureRepositoryClient)
                .findByEntityAndEventAndDocumentType(expectedRequest);
    }

    @Test
    void obtainDocumentSignatures_documentNotFound() {
        when(documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtainDocumentSignatures(documentTypes, entity, originId, "TRADE"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void obtainDocumentSignatures_multipleDocuments() {
        ProductDocumentParameters doc2 = ProductDocumentParameters.builder()
                .documentType("DOC2")
                .build();
        
        DocumentSignature signature2 = DocumentSignature.builder()
                .documentId("DOC2")
                .documentType("DOC2")
                .build();

        when(documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(any()))
                .thenReturn(Mono.just(documentSignature))
                .thenReturn(Mono.just(signature2));

        StepVerifier.create(useCase.obtainDocumentSignatures(List.of(documentParameter, doc2), 
                entity, originId, "TRADE"))
                .expectNext(List.of(documentSignature, signature2))
                .verifyComplete();
    }
}