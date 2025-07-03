package com.acelera.rest.fx.infrastructure.adapter.rsocket.controller;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.port.TradeSignatureClient;
import com.acelera.broker.rest.RestManyBrokerConfig;
import com.acelera.rest.fx.domain.port.FxCaller;
import com.acelera.rsocket.RSocketHeadersCustomizerAutoConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest(classes = { TradeSignatureRSocketController.class, RestManyBrokerConfig.class,
        RSocketRequesterAutoConfiguration.class, RSocketMessagingAutoConfiguration.class,
        JacksonAutoConfiguration.class, RSocketHeadersCustomizerAutoConfig.class,
        RSocketStrategiesAutoConfiguration.class, RSocketServerAutoConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TradeSignatureRSocketControllerTest {
    private static final int PORT = ThreadLocalRandom.current().nextInt(1111, 9999);

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("services.conac-springboot-rest-service.rsocket.host", () -> "localhost");
        registry.add("services.conac-springboot-rest-service.rsocket.port", () -> PORT);
        registry.add("spring.rsocket.server.port", () -> PORT);
    }

    private @MockitoBean FxCaller service;
    private @Qualifier("tradeSignatureRestClient2") @Autowired TradeSignatureClient client;

    @Test
    void testGetTradeSignature() {

        var response = GetTradeSignatureResponse.builder().tradeSignatureId(9876L).build();
        var locale = ArgumentCaptor.forClass(Locale.class);
        var entity = ArgumentCaptor.forClass(String.class);
        var request= ArgumentCaptor.forClass(GetTradeSignatureRequestParameter.class);

        when(service.getTradeSignature(locale.capture(), entity.capture(), request.capture()))
                .thenReturn(Mono.just(response));

        client.getTradeSignature(request.capture()).as(StepVerifier::create).expectNext(response).verifyComplete();
    }
}
