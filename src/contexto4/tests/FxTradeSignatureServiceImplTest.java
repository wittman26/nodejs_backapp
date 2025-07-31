package com.acelera.integration.rest.application.service;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.StartSignatureRequest;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.StartSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.broker.fx.domain.port.TradeSignatureClient;
import com.acelera.error.CustomErrorException;
import com.acelera.locale.LocaleConstants;
import com.acelera.locale.MessageSourceHolder;
import io.rsocket.exceptions.ApplicationErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FxTradeSignatureServiceImplTest {
    private @InjectMocks FxTradeSignatureServiceImpl impl;
    private @Mock TradeSignatureClient client;
    private @Mock ServerHttpRequest serverHttpRequest;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private final String GET_EXCEPTION = "TradeSignature exception";

    private final String GET_EXCEPTION_MESSAGE = "Se produjo un error en TradeSignature";

    @BeforeEach
    void setup() {
        var ms = new StaticMessageSource();
        ms.addMessage(GET_EXCEPTION, LocaleConstants.DEFAULT_LOCALE,
                GET_EXCEPTION_MESSAGE);
        MessageSourceHolder.setMessageSource(ms);
    }

    @Test
    void testPutTradeSignature() {
        var response = PODAM_FACTORY.manufacturePojo(TradeSignatureResponse.class);

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.updateTradeSignature(any())).thenReturn(Mono.just(response));

        impl.updateTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create).expectNext(response)
                .verifyComplete();
    }

    @Test
    void testPutTradeSignature_whenException() {

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.updateTradeSignature(any())).thenReturn(Mono.error(new ApplicationErrorException(GET_EXCEPTION)));

        impl.updateTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create)
                .expectErrorSatisfies(x -> assertThat(x)
                        .isInstanceOf(CustomErrorException.class).hasMessageContaining(GET_EXCEPTION_MESSAGE))
                .verify();

    }

    @Test
    void testPostTradeSignature_whenException() {

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.updateTradeSignature(any())).thenReturn(Mono.error(new ApplicationErrorException(GET_EXCEPTION)));

        impl.updateTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create)
                .expectErrorSatisfies(x -> assertThat(x)
                        .isInstanceOf(CustomErrorException.class).hasMessageContaining(GET_EXCEPTION_MESSAGE))
                .verify();

    }

    @Test
    void testGetTradeSignature() {
        var response = PODAM_FACTORY.manufacturePojo(GetTradeSignatureResponse.class);

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(GetTradeSignatureRequestParameter.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.getTradeSignature(any())).thenReturn(Mono.just(response));

        impl.getTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create).expectNext(response)
                .verifyComplete();
    }

    @Test
    void testGetTradeSignature_whenException() {

        var response = PODAM_FACTORY.manufacturePojo(StartSignatureResponse.class);

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(StartSignatureRequest.class);
        var originId = 123L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.postStartSignatureWorkflow(any())).thenReturn(Mono.just(response));

        impl.postStartSignatureWorkflow(entity, locale, request, originId, serverHttpRequest).as(StepVerifier::create).expectNext(response)
                .verifyComplete();

    }
}
