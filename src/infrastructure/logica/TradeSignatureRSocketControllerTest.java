package com.acelera.fx.db.infrastructure.adapter.rsocket.controller;

import com.acelera.broker.fx.db.FxDbBrokerConfig;
import com.acelera.broker.fx.db.domain.dto.MarginFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.shared.RSocketRequesterBuilderLoggerAutoConfig;
import com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest(classes = {TradeSignatureRSocketController.class,  FxDbBrokerConfig.class,
        RSocketRequesterAutoConfiguration.class, RSocketMessagingAutoConfiguration.class, JacksonAutoConfiguration.class,
        RSocketHeadersCustomizerAutoConfig.class, RSocketRequesterBuilderLoggerAutoConfig.class,
        RSocketStrategiesAutoConfiguration.class, RSocketServerAutoConfiguration.class })
@WithMockCustomUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TradeSignatureRSocketControllerTest {
    private static final int PORT = RandomUtils.nextInt(1111, 9999);
    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private @MockBean TradeSignatureRepository repository;
    private @Autowired
    @Qualifier("tradeSignatureRepositoryClient") TradeSignatureRepositoryClient client;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("services.conac-springboot-fx-db-service.rsocket.host", () -> "localhost");
        registry.add("services.conac-springboot-fx-db-service.rsocket.port", () -> PORT);
        registry.add("spring.rsocket.server.port", () -> PORT);
    }

    @Test
    void testSave() {
        var tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        when(repository.save(any(TradeSignature.class))).thenReturn(tradeSignature);

        client
                .save(tradeSignature).as(StepVerifier::create).assertNext(value -> assertThat(value)
                        .as("save tradeSignature").usingRecursiveComparison()
                        .ignoringFieldsMatchingRegexes(".*usumodi", ".*fecmodi", ".*usualta", ".*fecalta")
                        .isEqualTo(tradeSignature))
                .verifyComplete();
    }

    @Test
    void testFind() {

        var request = PODAM_FACTORY.manufacturePojo(TradeSignatureFindRequest.class);
        var response = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(repository.find(request)).thenReturn(Optional.of(response));

        client.find(request).as(StepVerifier::create).expectNext(response).verifyComplete();
    }

@Test
void testSave_shouldEmitError_whenRepositoryThrows() {
    var tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
    when(repository.save(any(TradeSignature.class))).thenThrow(new RuntimeException("DB error"));

    client.save(tradeSignature)
            .as(StepVerifier::create)
            .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("DB error"))
            .verify();
}    

@Test
void testFind_shouldReturnEmpty_whenNotFound() {
    var request = PODAM_FACTORY.manufacturePojo(TradeSignatureFindRequest.class);
    when(repository.find(request)).thenReturn(Optional.empty());

    client.find(request)
            .as(StepVerifier::create)
            .expectComplete()
            .verify();
}

@Test
void testSave_shouldCallRepositoryOnce() {
    var tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
    when(repository.save(any(TradeSignature.class))).thenReturn(tradeSignature);

    client.save(tradeSignature)
            .as(StepVerifier::create)
            .expectNext(tradeSignature)
            .verifyComplete();

    verify(repository, times(1)).save(tradeSignature);
}

@Test
void testFind_shouldCallRepositoryOnce() {
    var request = PODAM_FACTORY.manufacturePojo(TradeSignatureFindRequest.class);
    var response = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
    when(repository.find(request)).thenReturn(Optional.of(response));

    client.find(request)
            .as(StepVerifier::create)
            .expectNext(response)
            .verifyComplete();

    verify(repository, times(1)).find(request);
}
}
