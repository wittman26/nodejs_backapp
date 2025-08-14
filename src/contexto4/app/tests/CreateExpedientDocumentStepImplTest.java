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

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientDocumentStepImplTest {

    @Mock
    private ProductDocumentsService productDocumentsService;

    @InjectMocks
    private CreateExpedientDocumentStepImpl step;

    private final Locale locale = Locale.getDefault();
    private final String entity = "0049";
    private final String productId = "PROD1";
    private final List<ProductDocumentParameters> documentTypes = List.of(
        ProductDocumentParameters.builder()
            .product(productId)
            .documentType("DOC1")
            .build(),
        ProductDocumentParameters.builder()
            .product(productId)
            .documentType("DOC2")
            .build()
    );

    @BeforeEach
    void setUp() {
        when(productDocumentsService.findProductDocumentType(entity, locale, productId))
            .thenReturn(Flux.fromIterable(documentTypes));
    }

    @Test
    void obtainDocumentTypes_success() {
        StepVerifier.create(step.obtainDocumentTypes(entity, locale, productId))
            .expectNext(documentTypes)
            .verifyComplete();

        verify(productDocumentsService).findProductDocumentType(entity, locale, productId);
    }

    @Test
    void obtainDocumentTypes_empty() {
        when(productDocumentsService.findProductDocumentType(entity, locale, productId))
            .thenReturn(Flux.empty());

        StepVerifier.create(step.obtainDocumentTypes(entity, locale, productId))
            .expectErrorMatches(e -> e instanceof RuntimeException 
                && e.getMessage().equals("No se encontraron tipos de documento para el producto: " + productId))
            .verify();
    }

    @Test
    void obtainDocumentTypes_error() {
        when(productDocumentsService.findProductDocumentType(entity, locale, productId))
            .thenReturn(Flux.error(new RuntimeException("Error finding documents")));

        StepVerifier.create(step.obtainDocumentTypes(entity, locale, productId))
            .expectErrorMatches(e -> e instanceof RuntimeException 
                && e.getMessage().equals("Error finding documents"))
            .verify();
    }
}
