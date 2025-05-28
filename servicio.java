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
    void testFind() {
        var productId = "FW";

        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);

        when(client.findProductDocumentParameters(productId)).thenReturn(Flux.just(response));

        var flux = impl.findProductDocumentType(
                LocaleConstants.ENTITY_HEADER,
                LocaleConstants.DEFAULT_LOCALE,
                productId);
        StepVerifier.create(flux).expectNext(response).verifyComplete();
    }
}
