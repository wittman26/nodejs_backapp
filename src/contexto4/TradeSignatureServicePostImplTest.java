package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;

import java.util.Locale;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServicePostImplTest {

    @Mock
    ProductDocumentParametersRepositoryClient productRepository;

    @InjectMocks
    TradeSignatureServicePostImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testFlujoExitoso() {
        ProductDocumentParameters doc = new ProductDocumentParameters();
        doc.setDocumentType("CONTRATO");
        doc.setDocumentalTypeDoc("TIPO");
        doc.setIsPrecontractual("NO");

        when(productRepository.findProductDocumentParameters(any(ProductDocumentParametersRequest.class)))
                .thenReturn(Flux.just(doc));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);

        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow("ENTITY", Locale.getDefault(), 1L, request);

        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.getExpedientId() != null)
                .verifyComplete();
    }

    @Test
    void testErrorGeneracionDocumentacion() {
        ProductDocumentParameters doc = new ProductDocumentParameters();
        doc.setDocumentType("ERROR"); // Simula error

        when(productRepository.findProductDocumentParameters(any(ProductDocumentParametersRequest.class)))
                .thenReturn(Flux.just(doc));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);

        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow("ENTITY", Locale.getDefault(), 1L, request);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("Error de generación de documentación"))
                .verify();
    }

    @Test
    void testErrorGeneracionExpediente() {
        ProductDocumentParameters doc = new ProductDocumentParameters();
        doc.setDocumentType("CONTRATO");

        when(productRepository.findProductDocumentParameters(any(ProductDocumentParametersRequest.class)))
                .thenReturn(Flux.just(doc));

        StartSignatureRequestDto request = PODAM_FACTORY.manufacturePojo(StartSignatureRequestDto.class);

        // originId negativo para simular error en expediente
        Mono<StartSignatureResponseDto> result = impl.startSignatureWorkflow("ENTITY", Locale.getDefault(), -1L, request);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e.getMessage().contains("Error de generación de expediente"))
                .verify();
    }    
}
