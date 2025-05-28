@Controller
@RequiredArgsConstructor
public class ProductDocumentParametersRSocketController implements ProductDocumentParametersRepositoryClient{
    private final ProductDocumentParametersRepository repository;

    @Override
    public Flux<ProductDocumentParameters> findProductDocumentParameters(ProductDocumentParametersRequest request) {
        return Flux.fromIterable(repository.findProductDocumentParameters(request.getEntity(), request.getProductId()));
    }
}



package com.acelera.fx.db.infrastructure.adapter.rsocket.controller;

import com.acelera.broker.fx.db.FxDbBrokerConfig;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.broker.shared.RSocketRequesterBuilderLoggerAutoConfig;
import com.acelera.fx.db.domain.port.persistence.ProductDocumentParametersRepository;
import com.acelera.locale.LocaleConstants;
import com.acelera.rsocket.RSocketHeadersCustomizerAutoConfig;
import com.acelera.test.WithMockCustomUser;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest(classes = { ProductDocumentParametersRSocketController.class, FxDbBrokerConfig.class,
        RSocketRequesterAutoConfiguration.class, RSocketMessagingAutoConfiguration.class, JacksonAutoConfiguration.class,
        RSocketHeadersCustomizerAutoConfig.class, RSocketRequesterBuilderLoggerAutoConfig.class,
        RSocketStrategiesAutoConfiguration.class, RSocketServerAutoConfiguration.class })
@WithMockCustomUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProductDocumentParametersRSocketControllerTest {
    private static final int PORT = RandomUtils.nextInt(1111, 9999);
    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final String PRODUCT_ID = "FW";

    private @MockBean ProductDocumentParametersRepository service;
    private @Qualifier("productDocumentParametersRepositoryClient") @Autowired ProductDocumentParametersRepositoryClient client;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("services.conac-springboot-fx-db-service.rsocket.host", () -> "localhost");
        registry.add("services.conac-springboot-fx-db-service.rsocket.port", () -> PORT);
        registry.add("spring.rsocket.server.port", () -> PORT);
    }

    @Test
    void testFind() {

        var request = PODAM_FACTORY.manufacturePojo(ProductDocumentParametersRequest.class);
        
        List<ProductDocumentParameters> response = new ArrayList<>();
        response.add(PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class));

        when(service.findProductDocumentParameters(LocaleConstants.ENTITY_0049, PRODUCT_ID)).thenReturn(response);

        client.findProductDocumentParameters(request).as(StepVerifier::create)
                .expectNext(response.get(0)).verifyComplete();
    }

}