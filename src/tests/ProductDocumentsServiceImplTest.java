package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum;
import com.acelera.locale.LocaleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductDocumentsServiceImplTest {
    @InjectMocks
    private ProductDocumentsServiceImpl impl;

    @Mock
    private ProductDocumentParametersRepositoryClient client;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    private static final String PRODUCT_ID = "FW";
    private static final ProductDocumentParametersRequest REQUEST = new ProductDocumentParametersRequest(PRODUCT_ID, LocaleConstants.ENTITY_0049);

    @Test
    void testFindProductDocumentType_ok() {
        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        response.setDocumentType(String.valueOf(TypeEnum.KD));
        when(client.findProductDocumentParameters(REQUEST)).thenReturn(Flux.just(response));

        var flux = impl.findProductDocumentType(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID);
        StepVerifier.create(flux)
                .expectNextMatches(dto -> dto.getDocumentalTypeDoc() != null && dto.getDocumentType().equals(response.getDocumentType()))
                .verifyComplete();
    }

    @Test
    void testFindProductDocumentType_empty() {
        when(client.findProductDocumentParameters(REQUEST)).thenReturn(Flux.empty());
        var flux = impl.findProductDocumentType(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID);
        StepVerifier.create(flux).verifyComplete();
    }

    @Test
    void testFindProductDocumentType_invalidEnum() {
        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        response.setDocumentType("INVALID");
        when(client.findProductDocumentParameters(REQUEST)).thenReturn(Flux.just(response));
        var flux = impl.findProductDocumentType(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID);
        StepVerifier.create(flux)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
