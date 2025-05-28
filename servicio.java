@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDocumentsServiceImpl implements ProductDocumentsService {

    private final ProductDocumentParametersRepositoryClient client;

    @Override
    public Flux<DocumentTypeResponse> findProductDocumentType(String entity, Locale locale, String productId) {
        Flux<ProductDocumentParameters> response = client.findProductDocumentParameters(productId);
        log.info("findProductDocumentType - {}", response);
        return response.map(ProductDocumentsMapper.INSTANCE::toDocumentTypeResponse);
    }
}

package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
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

    @Test
    void testFindProductDocumentType_ok() {
        var productId = "FW";
        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        // Aseguramos que el documentType sea válido para el enum
        response.setDocumentType("KD");
        when(client.findProductDocumentParameters(productId)).thenReturn(Flux.just(response));

        var flux = impl.findProductDocumentType(
                LocaleConstants.ENTITY_HEADER,
                LocaleConstants.DEFAULT_LOCALE,
                productId);
        StepVerifier.create(flux)
                .expectNextMatches(dto -> dto.getType() != null && dto.getType().name().equals(response.getDocumentType()))
                .verifyComplete();
    }

    @Test
    void testFindProductDocumentType_empty() {
        var productId = "FW";
        when(client.findProductDocumentParameters(productId)).thenReturn(Flux.empty());
        var flux = impl.findProductDocumentType(
                LocaleConstants.ENTITY_HEADER,
                LocaleConstants.DEFAULT_LOCALE,
                productId);
        StepVerifier.create(flux).verifyComplete();
    }

    @Test
    void testFindProductDocumentType_invalidEnum() {
        var productId = "FW";
        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        response.setDocumentType("INVALID");
        when(client.findProductDocumentParameters(productId)).thenReturn(Flux.just(response));
        var flux = impl.findProductDocumentType(
                LocaleConstants.ENTITY_HEADER,
                LocaleConstants.DEFAULT_LOCALE,
                productId);
        StepVerifier.create(flux)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
